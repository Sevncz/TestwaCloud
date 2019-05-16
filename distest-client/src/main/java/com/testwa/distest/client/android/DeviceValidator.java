package com.testwa.distest.client.android;

import lombok.extern.slf4j.Slf4j;

/**
 * @author wen
 * @create 2019-05-15 19:02
 */
@Slf4j
public class DeviceValidator {
    private static final String IS_REMOTE_FLAG = ":";

    public static boolean isLocalDevice(String deviceId) {
        return !deviceId.contains(IS_REMOTE_FLAG);
    }
}
