package com.testwa.distest.client2.support.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.IDevice;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wen
 * @create 2019-05-08 14:58
 */
@Slf4j
@Data
public class AndroidDeviceStore {
    private AndroidDebugBridge bridge;

    private static final Map<String, IDevice> I_DEVICE_MAP = new ConcurrentHashMap<>();

    private boolean shouldKeepAdbAlive = false;

    /**
     * 单例
     */
    private static AndroidDeviceStore INSTANCE = null;
    /**
     * 私有构造函数
     */
    private AndroidDeviceStore() {

    }
    public static AndroidDeviceStore getInstance() {
        return AndroidDeviceStore.DeviceStoreHolder.instance;
    }

    public void initAndroidDevices(boolean shouldKeepAdbAlive) {
        DdmPreferences.setInitialThreadUpdate(true);
        DdmPreferences.setInitialHeapUpdate(true);
        this.initializeAdbConnection();
    }

    protected void initializeAdbConnection() {
        try {
            AndroidDebugBridge.init(true);
        } catch (IllegalStateException var6) {
            if (!this.shouldKeepAdbAlive) {
                log.error("The IllegalStateException is not a show stopper. It has been handled. This is just debug spew. Please proceed.", var6);
                throw new RuntimeException("ADB init failed", var6);
            }
        }

        this.bridge = AndroidDebugBridge.getBridge();
        if (this.bridge == null) {
            this.bridge = AndroidDebugBridge.createBridge(AndroidSdk.adb().getAbsolutePath(), false);
        }

        long timeout = System.currentTimeMillis() + 60000L;

        while(!this.bridge.hasInitialDeviceList() && System.currentTimeMillis() < timeout) {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException var5) {
                throw new RuntimeException(var5);
            }
        }

        IDevice[] devices = this.bridge.getDevices();
        log.info("initialDeviceList size {}", devices.length);

        for(int i = 0; i < devices.length; ++i) {
            log.info("devices state: {},{} ", devices[i].getName(), devices[i].getState());
            put(devices[i]);
        }
        AndroidDebugBridge.addDeviceChangeListener(new DeviceChangeListener());
    }

    public void shutdown() {
        if (!this.shouldKeepAdbAlive) {
            AndroidDebugBridge.disconnectBridge();
            AndroidDebugBridge.terminate();
        }

        log.info("stopping Device Manager");
    }

    public void shutdownForcely() {
        AndroidDebugBridge.disconnectBridge();
        AndroidDebugBridge.terminate();
    }

    static class DeviceStoreHolder {
        static final AndroidDeviceStore instance = init();

        DeviceStoreHolder() {
        }

        static AndroidDeviceStore init() {
            AndroidDeviceStore instance = new AndroidDeviceStore();
            instance.initAndroidDevices(false);
            return instance;
        }
    }


    public void put(IDevice iDevice) {
        if(iDevice != null) {
            I_DEVICE_MAP.put(iDevice.getSerialNumber(), iDevice);
        }
    }

    public void remove(IDevice iDevice) {
        if(iDevice != null) {
            I_DEVICE_MAP.remove(iDevice.getSerialNumber());
        }
    }

    public void remove(String deviceId) {
        if(StringUtils.isNotEmpty(deviceId)) {
            I_DEVICE_MAP.remove(deviceId);
        }
    }

    public IDevice get(String deviceId) {
        if(StringUtils.isNotEmpty(deviceId)) {
            return I_DEVICE_MAP.get(deviceId);
        }
        return null;
    }

    public void putAll(IDevice[] devices) {
        if(devices != null && devices.length > 0) {
            for(int i=0; i<devices.length; i++) {
                put(devices[i]);
            }
        }
    }


}
