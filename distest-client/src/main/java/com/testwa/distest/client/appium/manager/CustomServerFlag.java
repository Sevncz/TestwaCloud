package com.testwa.distest.client.appium.manager;

import io.appium.java_client.service.local.flags.ServerArgument;

/**
 * Created by wen on 16/5/28.
 */
public enum CustomServerFlag implements ServerArgument {
    TESTCASE_LOG_ID("--testcaselogId"),
    TESTWA_DEVICE_ID("--testwaDeviceId"),
    INSTALL_APP("--installapp"),
    SCREEN_PATH("--screenpath"),
    SET_DEVICE("-U");

    private final String arg;

    private CustomServerFlag(String arg) {
        this.arg = arg;
    }

    public String getArgument() {
        return this.arg;
    }
}
