/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.testwa.distest.client.component.wda.remote;

import com.testwa.distest.client.component.wda.driver.DriverCapabilities;
import com.testwa.distest.client.component.wda.exception.WebDriverAgentException;
import com.testwa.distest.client.util.CommandLineExecutor;
import com.testwa.distest.client.component.wda.support.XCodeBuilder;
import com.testwa.distest.client.util.PortUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.openqa.selenium.net.UrlChecker;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.*;


@Slf4j
public class WebDriverAgentRunner {
    private static final String WDA_BASE_URL = "http://localhost";
    private static final String WDA_STATE_FIELD = "state";
    private static final int WDA_AGENT_PORT = 8100;
    private static final int WDA_SCREEN_PORT = 9100;
    private static final int DEFAULT_LAUNCH_TIMEOUT = 60;
    private static final String IPROXY = "/usr/local/bin/iproxy";

    private DriverCapabilities capabilities;
//    private CommandExecutor commandExecutor;
    private StartedProcess wdaProcess;
    private StartedProcess iproxyProcess;
    private StartedProcess iproxyScreenProcess;

    private Integer iproxyPort;
    private Integer iproxyScreenPort;

    public WebDriverAgentRunner(DriverCapabilities capabilities) {
        this.capabilities = capabilities;
//        this.commandExecutor = new WDACommandExecutor(getWdaUrl());
    }

    public void start() {
        boolean startNewProcess = Optional.ofNullable(capabilities.getCapability(DriverCapabilities.Key.PREBUILT_WDA))
                .map(k -> !Boolean.valueOf(k))
                .orElse(true);
        if (startNewProcess) {
            String udid = capabilities.getCapability(DriverCapabilities.Key.DEVICE_ID);
            log.info("[Start WebDriverAgent] {}", udid);
            wdaProcess = new XCodeBuilder()
                    .setWdaPath(capabilities.getCapability(DriverCapabilities.Key.WDA_PATH))
                    .setPlatform(capabilities.getCapability(DriverCapabilities.Key.PLATFORM))
                    .setDeviceName(capabilities.getCapability(DriverCapabilities.Key.DEVICE_NAME))
                    .setDeviceId(capabilities.getCapability(DriverCapabilities.Key.DEVICE_ID))
                    .setOsVersion(capabilities.getCapability(DriverCapabilities.Key.OS_VERSION))
                    .setLog(false)
                    .build();

            try {
                this.iproxyPort = PortUtil.getAvailablePort();
                this.iproxyProcess = CommandLineExecutor.asyncExecute(new String[]{IPROXY, String.valueOf(this.iproxyPort), String.valueOf(WDA_AGENT_PORT), capabilities.getCapability(DriverCapabilities.Key.DEVICE_ID)});
                this.iproxyScreenPort = PortUtil.getAvailablePort();
                this.iproxyScreenProcess = CommandLineExecutor.asyncExecute(new String[]{IPROXY, String.valueOf(this.iproxyScreenPort), String.valueOf(WDA_SCREEN_PORT), capabilities.getCapability(DriverCapabilities.Key.DEVICE_ID)});
            } catch (IOException e) {
                e.printStackTrace();
            }

            Thread checkThread = new Thread(() -> {
                int timeout = Optional.ofNullable(capabilities.getCapability(DriverCapabilities.Key.LAUNCH_TIMEOUT))
                        .map(Integer::valueOf)
                        .orElse(DEFAULT_LAUNCH_TIMEOUT);
                waitForReachability(getWdaUrl(), timeout);
//                checkStatus();
            });
            checkThread.start();

        } else {
            log.info("Use existing WebDriverAgent process.");
        }
    }

    public void stop() {
        log.info("Stop WebDriverAgent.");
//        CommandLineExecutor.processQuit(iproxyProcess);

        String udid = capabilities.getCapability(DriverCapabilities.Key.DEVICE_ID);
        if(StringUtils.isNotBlank(udid)) {
            // kill 进程
            try {
                // kill iproxy
                new ProcessExecutor()
                        .command("/bin/sh","-c","ps aux | grep iproxy | grep " + udid + " | awk {'print $2'} | xargs kill -9")
                        .redirectOutput(new LogOutputStream() {
                            @Override
                            protected void processLine(String line) {
                                log.info(line);
                            }
                        }).execute();

                new ProcessExecutor()
                        .command("/bin/sh","-c","ps aux | grep xcodebuild | grep " + udid + " | awk {'print $2'} | xargs kill -9")
                        .redirectOutput(new LogOutputStream() {
                            @Override
                            protected void processLine(String line) {
                                log.info(line);
                            }
                        }).execute();

            } catch (IOException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
        }
//        if(this.iproxyPort != null) {
//            IProxyPortProvider.pushPort(this.iproxyPort);
//        }
//        if(this.iproxyScreenPort != null) {
//            IProxyPortProvider.pushPort(this.iproxyScreenPort);
//        }
//        CommandLineExecutor.processQuit(wdaProcess);
    }

    public URL getWdaUrl() {

        String urlStr = Optional.ofNullable(capabilities.getCapability(DriverCapabilities.Key.DEVICE_IP))
                .map(url -> String.format("http://%s:%s", url, iproxyPort))
                .orElse(String.format("%s:%s", WDA_BASE_URL, iproxyPort));
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            throw new WebDriverAgentException("Url syntax is malformed: " + urlStr);
        }
    }

    public boolean waitForReachability(URL url, int timeout) {
        try {
            String urlStr = url.toString() + "/" + WDACommand.STATUS;

            new UrlChecker().waitUntilAvailable(timeout, TimeUnit.SECONDS, new URL(urlStr));
            return true;
        } catch (UrlChecker.TimeoutException | MalformedURLException e) {
            return false;
        }
    }

    private boolean isUrlReachable(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = this.connectToUrl(url);
            return connection.getResponseCode() == HttpStatus.SC_OK;
        } catch (IOException e) {
            log.debug("Wait for WDA to be available: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }

    /**
     * @Description:
        {"value":{
        "build":{
        "productBundleIdentifier":"com.facebook.WebDriverAgentRunner",
        "time":"Jun 24 2019 15:16:52"
        },
        "device":{
        "name":"W e N - iPhone",
        "udid":"8e9e4b90bf9f8ad4d544b5e3d9d6b5940e0912e4"
        },
        "ios":{
        "ip":null,
        "simulatorVersion":"12.3.1"
        },
        "os":{
        "name":"iOS",
        "sdkVersion":"12.2",
        "version":"12.3.1"
        },
        "state":"success"
        },
        "sessionId":"29E1D582-D4EA-47F7-B0D4-C38D35CBE385",
        "status":0
        }
     * @Param: []
     * @Return: void
     * @Author wen
     * @Date 2019-06-24 15:21
     */
    public void checkStatus() {
//        RemoteResponse response = commandExecutor.execute(WDACommand.STATUS);
//        String state = (String) new ResponseValueConverter(response).toMap().get(WDA_STATE_FIELD);
//        log.info("[Check WebDriverAgent status] state: {}", state);
//        if (!"success".equals(state)) {
//            throw new WebDriverAgentException("WDA returned error state: " + state);
//        }
    }

    private HttpURLConnection connectToUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(500);
        connection.setReadTimeout(1000);
        connection.connect();
        return connection;
    }

    public Integer getScreenPort() {
        return this.iproxyScreenPort;
    }
}
