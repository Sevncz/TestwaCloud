package com.testwa.distest.client.device.driver;

public class IDeviceRemoteDriverFactory {

    public static IDeviceRemoteControlDriver createIOSDriver(IDeviceRemoteControlDriverCapabilities capabilities) {
        return new IOSRemoteControlDriver(capabilities);
    }
    public static IDeviceRemoteControlDriver createAndroidDriver(IDeviceRemoteControlDriverCapabilities capabilities) {
        return new AndroidRemoteControlDriver(capabilities);
    }
}