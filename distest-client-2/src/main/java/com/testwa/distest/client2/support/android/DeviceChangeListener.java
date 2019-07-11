package com.testwa.distest.client2.support.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备监听器
 */
@Slf4j
public class DeviceChangeListener implements AndroidDebugBridge.IDeviceChangeListener {

    @Override
    public void deviceConnected(IDevice device) {
        AndroidDeviceStore.getInstance().put(device);
    }

    @Override
    public void deviceDisconnected(IDevice device) {
        AndroidDeviceStore.getInstance().remove(device);
    }

    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        if (device.isOnline()) {
//            log.debug("设备 {} 状态变更： 在线 {}", device.getName(), changeMask);
        } else {
            log.info("设备 {} 状态变更： 离线 {}", device.getName(), changeMask);
            AndroidDeviceStore.getInstance().remove(device);
        }
    }

}