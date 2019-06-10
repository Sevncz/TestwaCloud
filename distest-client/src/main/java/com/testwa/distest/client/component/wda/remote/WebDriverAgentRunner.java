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

import com.testwa.distest.client.command.WdaProcessListener;
import com.testwa.distest.client.component.port.IProxyPortProvider;
import com.testwa.distest.client.component.wda.driver.CommandExecutor;
import com.testwa.distest.client.component.wda.driver.DriverCapabilities;
import com.testwa.distest.client.component.wda.exception.WebDriverAgentException;
import com.testwa.distest.client.util.CommandLineExecutor;
import com.testwa.distest.client.component.wda.support.ResponseValueConverter;
import com.testwa.distest.client.component.wda.support.XCodeBuilder;
import com.testwa.distest.client.ios.IOSDeviceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.openqa.selenium.net.UrlChecker;
import org.zeroturnaround.exec.StartedProcess;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Slf4j
public class WebDriverAgentRunner {
    private static final String WDA_BASE_URL = "http://localhost";
    private static final String WDA_STATE_FIELD = "state";
    private static final int WDA_AGENT_PORT = 8100;
    private static final int DEFAULT_LAUNCH_TIMEOUT = 60;
    private static final String IPROXY = "/usr/local/bin/iproxy";

    private DriverCapabilities capabilities;
    private CommandExecutor commandExecutor;
    private StartedProcess wdaProcess;
    private StartedProcess iproxyProcess;

    private WdaProcessListener wdaProcessListener;
    private boolean stop;
    private int iproxyPort;

    public WebDriverAgentRunner(DriverCapabilities capabilities) {
        this.capabilities = capabilities;
        this.stop = false;
        this.iproxyPort = IProxyPortProvider.pullPort();
        this.commandExecutor = new WDACommandExecutor(getWdaUrl());
        this.wdaProcessListener = new WdaProcessListener(this);
        log.info("iproxy port {}", iproxyPort);
    }

    public void start() {
        boolean startNewProcess = Optional.ofNullable(capabilities.getCapability(DriverCapabilities.Key.PREBUILT_WDA))
                .map(k -> !Boolean.valueOf(k))
                .orElse(true);
        if (startNewProcess) {
            String udid = capabilities.getCapability(DriverCapabilities.Key.DEVICE_ID);
            while(IOSDeviceUtil.isOnline(udid)){
                if(IOSDeviceUtil.isUndetermined(udid)) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }else{
                    break;
                }
            }

            log.info("Start WebDriverAgent.");
            wdaProcess = new XCodeBuilder()
                    .setWdaPath(capabilities.getCapability(DriverCapabilities.Key.WDA_PATH))
                    .setPlatform(capabilities.getCapability(DriverCapabilities.Key.PLATFORM))
                    .setDeviceName(capabilities.getCapability(DriverCapabilities.Key.DEVICE_NAME))
                    .setDeviceId(capabilities.getCapability(DriverCapabilities.Key.DEVICE_ID))
                    .setOsVersion(capabilities.getCapability(DriverCapabilities.Key.OS_VERSION))
                    .setLog(false)
                    .addListener(this.wdaProcessListener)
                    .build();
            iproxyProcess = CommandLineExecutor.asyncExecute(new String[]{IPROXY, String.valueOf(this.iproxyPort), String.valueOf(WDA_AGENT_PORT), udid});
        } else {
            log.info("Use existing WebDriverAgent process.");
        }

        int timeout = Optional.ofNullable(capabilities.getCapability(DriverCapabilities.Key.LAUNCH_TIMEOUT))
                .map(Integer::valueOf)
                .orElse(DEFAULT_LAUNCH_TIMEOUT);

        waitForReachability(getWdaUrl(), timeout);
        checkStatus();
    }

    public void stop() {
        log.info("Stop WebDriverAgent.");
        this.stop = true;
        CommandLineExecutor.processQuit(wdaProcess);
        CommandLineExecutor.processQuit(iproxyProcess);
    }

    public boolean isStop() {
        return this.stop;
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

    private boolean waitForReachability(URL url, int timeout) {
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

    private void checkStatus() {
        log.info("Check WebDriverAgent status.");
        RemoteResponse response = commandExecutor.execute(WDACommand.STATUS);
        String state = (String) new ResponseValueConverter(response).toMap().get(WDA_STATE_FIELD);

        if (!"success".equals(state)) {
            stop();
            throw new WebDriverAgentException("WDA returned error state: " + state);
        }
    }

    private HttpURLConnection connectToUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(500);
        connection.setReadTimeout(1000);
        connection.connect();
        return connection;
    }
}
