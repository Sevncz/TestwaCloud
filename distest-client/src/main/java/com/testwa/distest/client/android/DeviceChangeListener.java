package com.testwa.distest.client.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.event.DeviceConnectedEvent;
import com.testwa.distest.client.event.DeviceDisconnectEvent;
import com.testwa.distest.client.event.DeviceOfflineEvent;
import com.testwa.distest.client.event.DeviceOnlineEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * 设备监听器
 */
@Slf4j
public class DeviceChangeListener implements AndroidDebugBridge.IDeviceChangeListener {

    @Override
    public void deviceConnected(IDevice device) {
        connect(device.getSerialNumber());
    }

    @Override
    public void deviceDisconnected(IDevice device) {
        disconnect(device.getSerialNumber());
    }

    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        if (device.isOnline()) {
            log.debug("设备 {} 状态变更： 在线 {}", device.getName(), changeMask);
            online(device.getSerialNumber());
        } else {
            log.info("设备 {} 状态变更： 离线 {}", device.getName(), changeMask);
            offline(device.getSerialNumber());
        }
    }

    private void connect(String deviceId) {
        ApplicationContext context = ApplicationContextUtil.getApplicationContext();
        context.publishEvent(new DeviceConnectedEvent(this, deviceId));
    }

    private void disconnect(String deviceId) {
        ApplicationContext context = ApplicationContextUtil.getApplicationContext();
        context.publishEvent(new DeviceDisconnectEvent(this, deviceId));
    }

    private void offline(String deviceId) {
        ApplicationContext context = ApplicationContextUtil.getApplicationContext();
        context.publishEvent(new DeviceOfflineEvent(this, deviceId));
    }

    private void online(String deviceId) {
        ApplicationContext context = ApplicationContextUtil.getApplicationContext();
        context.publishEvent(new DeviceOnlineEvent(this, deviceId));

    }

}