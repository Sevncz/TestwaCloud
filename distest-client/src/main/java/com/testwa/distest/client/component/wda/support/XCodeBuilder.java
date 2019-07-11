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

package com.testwa.distest.client.component.wda.support;

import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.component.wda.exception.WebDriverAgentException;
import com.testwa.distest.client.util.CommonProcessListener;
import lombok.extern.slf4j.Slf4j;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.listener.ProcessListener;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class XCodeBuilder {

    private static String COMMAND_NAME = "xcodebuild";
    private static String RUN_TYPE = "build-for-testing";
    private static String TEST_BUILD = "test-without-building";
    private static String TEST = "test";
    private static String USE_PORT = "USE_PORT";
    private static String OS_SCHEME = "WebDriverAgentRunner";
    private String wdaPath;
    private String platform;
    private String deviceName;
    private String deviceId;
    private String osVersion;
    private String usePort;
    private LogOutputStream logOutputStream;
    private List<ProcessListener> listeners = new ArrayList<>();

    public StartedProcess build() {
        log.info("[{}] Start xcode build process", deviceId);
        String[] commandLine = getCommand();
        CommonProcessListener processListener = new CommonProcessListener(String.join(" ", commandLine));
        try {
            ProcessExecutor executor = new ProcessExecutor()
                    .command(commandLine)
                    .readOutput(true);
            if(logOutputStream != null) {
                executor = executor.redirectOutput(logOutputStream);
            }
            if(listeners != null && !listeners.isEmpty()) {
                for (ProcessListener l : listeners) {
                    executor.addListener(l);
                }
            }else{
                executor.addListener(processListener);
            }
            return executor.start();
        } catch (IOException e) {
            throw new WebDriverAgentException(e);
        }
    }

    public XCodeBuilder setWdaPath(String wdaPath) {
        this.wdaPath = wdaPath;
        return this;
    }

    public XCodeBuilder setPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    public XCodeBuilder setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    public XCodeBuilder setOsVersion(String osVersion) {
        this.osVersion = osVersion;
        return this;
    }

    public XCodeBuilder setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public XCodeBuilder setLog(boolean isLog) {
        if(isLog) {
            this.logOutputStream = new XCodeLogOutputStream();
        }
        return this;
    }

    public XCodeBuilder setUsePort(int port) {
        this.usePort = String.valueOf(port);
        return this;
    }

    public XCodeBuilder addListener(ProcessListener listener) {
        listeners.add(listener);
        return this;
    }

    private String[] getCommand() {
        if (wdaPath == null) {
            throw new WebDriverAgentException("Unable to build WDA. Please, specify path to WebDriverAgent.xcodeproj.");
        }
        List<String> command = new ArrayList<>();
        command.add(COMMAND_NAME);
//        command.add(RUN_TYPE);
//        command.add(TEST_BUILD);
        command.add(CLProperty.PROJECT.getValue());
        command.add(wdaPath);
        command.add(CLProperty.SCHEME.getValue());
        command.add(OS_SCHEME);
        command.add(CLProperty.DESTINATION.getValue());
        command.add(new DestinationBuilder().build());
//        command.add(CLProperty.ALLOW_PROVISIONING_UPDATES.getValue());
        Path xcodePath = Paths.get(Constant.XCODEBUILD_CONFIG_DIR, this.deviceId);
        command.add(String.format("%s=%s", CLProperty.CONFIGURATION_BUILD_DIR.getValue(), xcodePath.toString()));
        command.add(String.format("%s=%s", USE_PORT, usePort));
        command.add(TEST);
        return command.toArray(new String[0]);
    }

    private enum CLProperty {
        PROJECT("-project"),
        DERIVED_DATA_PATH("-derivedDataPath"),
        DESTINATION("-destination"),
        XCODE_CONFIG("-xcconfig"),
        XCODE_TEST_RUN("-xctestrun"),
        SCHEME("-scheme"),
        ALLOW_PROVISIONING_UPDATES("-allowProvisioningUpdates"),
        CONFIGURATION_BUILD_DIR("CONFIGURATION_BUILD_DIR"),
        ;

        String value;

        CLProperty(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    private final class DestinationBuilder {
        private static final String NAME_KEY = "name";
        private static final String ID_KEY = "id";
        private static final String SEPARATOR = ",";
        private static final String GENERIC_ERR = "Unable to build remote destination.";

        String build() {
            StringBuilder builder = new StringBuilder();
            appendDeviceInfo(builder);
            return builder.toString();
        }

        private void appendDeviceInfo(StringBuilder builder) {
            if (deviceId != null) {
                builder.append(generateKeyValuePair(ID_KEY, deviceId));
            } else if (deviceName != null) {
                builder.append(generateKeyValuePair(NAME_KEY, deviceName));
            } else {
                throw new WebDriverAgentException(GENERIC_ERR + " Please, specify device id or name.");
            }
        }

        private String generateKeyValuePair(String key, String value) {
            return String.format("%s=%s", key, value);
        }
    }
}
