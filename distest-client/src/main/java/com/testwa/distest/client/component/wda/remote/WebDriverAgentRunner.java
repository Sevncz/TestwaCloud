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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


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

    private int iproxyPort;

    private ScheduledExecutorService wdaWatchDog;
    private ScheduledFuture<?> handle;

    private AtomicBoolean isStart = new AtomicBoolean(false);

    public WebDriverAgentRunner(DriverCapabilities capabilities) {
        this.capabilities = capabilities;
        this.iproxyPort = IProxyPortProvider.pullPort();
        this.commandExecutor = new WDACommandExecutor(getWdaUrl());
        this.iproxyProcess = CommandLineExecutor.asyncExecute(new String[]{IPROXY, String.valueOf(this.iproxyPort), String.valueOf(WDA_AGENT_PORT), capabilities.getCapability(DriverCapabilities.Key.DEVICE_ID)});
        this.wdaWatchDog = Executors.newSingleThreadScheduledExecutor();
        this.handle =this.wdaWatchDog.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if(isStart.get()){
                    if(wdaProcess == null || wdaProcess.getProcess() == null || !wdaProcess.getProcess().isAlive()) {
                        log.info("[WebDriverAgentRunner] 重启 xcodebuild");
                        start();
                    }
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
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

            log.info("[Start WebDriverAgent] {}", udid);
            wdaProcess = new XCodeBuilder()
                    .setWdaPath(capabilities.getCapability(DriverCapabilities.Key.WDA_PATH))
                    .setPlatform(capabilities.getCapability(DriverCapabilities.Key.PLATFORM))
                    .setDeviceName(capabilities.getCapability(DriverCapabilities.Key.DEVICE_NAME))
                    .setDeviceId(capabilities.getCapability(DriverCapabilities.Key.DEVICE_ID))
                    .setOsVersion(capabilities.getCapability(DriverCapabilities.Key.OS_VERSION))
                    .setLog(false)
                    .build();

        } else {
            log.info("Use existing WebDriverAgent process.");
        }

        int timeout = Optional.ofNullable(capabilities.getCapability(DriverCapabilities.Key.LAUNCH_TIMEOUT))
                .map(Integer::valueOf)
                .orElse(DEFAULT_LAUNCH_TIMEOUT);

        waitForReachability(getWdaUrl(), timeout);
        checkStatus();
        isStart.set(true);
    }

    public void stop() {
        log.info("Stop WebDriverAgent.");
        isStart.set(false);
        handle.cancel(true);
        this.wdaWatchDog.shutdown();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {

        }
        CommandLineExecutor.processQuit(wdaProcess);
        CommandLineExecutor.processQuit(iproxyProcess);
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
        RemoteResponse response = commandExecutor.execute(WDACommand.STATUS);
        String state = (String) new ResponseValueConverter(response).toMap().get(WDA_STATE_FIELD);
        log.info("[Check WebDriverAgent status] state: {}", state);
        if (!"success".equals(state)) {
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
