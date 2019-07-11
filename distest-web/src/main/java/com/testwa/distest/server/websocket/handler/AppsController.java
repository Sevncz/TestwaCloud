package com.testwa.distest.server.websocket.handler;

import com.google.protobuf.ByteString;
import com.testwa.distest.server.rpc.cache.CacheUtil;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.agent.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * see https://github.com/aosp-mirror/platform_packages_apps_settings/blob/master/AndroidManifest.xml
 */
@Slf4j
public class AppsController {

    private static void run(String deviceId, String cmd) {
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if (observer != null) {
            Message message = Message.newBuilder().setTopicName(Message.Topic.SHELL).setStatus("OK").setMessage(ByteString.copyFromUtf8(cmd)).build();
            observer.onNext(message);
        }
    }

    public static void openSetting(String deviceId, String activity) {
        run(deviceId, "am start -a android.intent.action.MAIN -n com.android.settings/.Settings\\$" + activity);
    }

    public static void openSettings(String deviceId) {
        run(deviceId, "am start -a android.intent.action.MAIN -n com.android.settings/.Settings");
    }

    public static void openWiFiSettings(String deviceId) {
        run(deviceId, "am start -a android.settings.WIFI_SETTINGS");
    }

    public static void openLocaleSettings(String deviceId) {
        openSetting(deviceId, "LocalePickerActivity");
    }

    public static void openIMESettings(String deviceId) {
        openSetting(deviceId, "KeyboardLayoutPickerActivity");
    }

    public static void openDisplaySettings(String deviceId) {
        openSetting(deviceId, "DisplaySettingsActivity");
    }

    public static void openDeviceInfo(String deviceId) {
        openSetting(deviceId, "DeviceInfoSettingsActivity");
    }

    public static void openManageApps(String deviceId) {
        run(deviceId, "am start -a android.settings.APPLICATION_SETTINGS");
    }

    public static void openRunningApps(String deviceId) {
        openSetting(deviceId, "RunningServicesActivity");
    }

    public static void openDeveloperSettings(String deviceId) {
        openSetting(deviceId, "DevelopmentSettingsActivity");
    }

}
