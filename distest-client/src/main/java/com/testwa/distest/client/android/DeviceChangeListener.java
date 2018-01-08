package com.testwa.distest.client.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.android.impl.DefaultHardwareDevice;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.control.client.MainSocket;
import com.testwa.distest.client.control.event.DeviceConnectedEvent;
import com.testwa.distest.client.control.event.DeviceDisconnectEvent;
import com.testwa.distest.client.control.event.DeviceOfflineEvent;
import com.testwa.distest.client.control.event.DeviceOnlineEvent;
import com.testwa.distest.client.grpc.Gvice;
import com.testwa.distest.client.model.TestwaDevice;
import com.testwa.distest.client.service.GrpcClientService;
import com.testwa.distest.client.task.TestwaScheduled;
import io.rpc.testwa.device.Device;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 设备监听器
 */
@Log4j2
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
            log.info("device {} online", device.getSerialNumber());
            online(device.getSerialNumber());
        } else {
            log.info("device {} offline", device.getSerialNumber());
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