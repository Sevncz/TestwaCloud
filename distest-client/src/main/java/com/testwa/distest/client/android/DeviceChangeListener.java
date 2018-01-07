package com.testwa.distest.client.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.android.impl.DefaultHardwareDevice;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.control.client.MainSocket;
import com.testwa.distest.client.control.event.DeviceConnectedEvent;
import com.testwa.distest.client.control.event.DeviceDisconnectEvent;
import com.testwa.distest.client.grpc.Gvice;
import com.testwa.distest.client.model.TestwaDevice;
import com.testwa.distest.client.service.GrpcClientService;
import com.testwa.distest.client.task.TestwaScheduled;
import io.rpc.testwa.device.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 设备监听器
 */
public class DeviceChangeListener implements AndroidDebugBridge.IDeviceChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(DeviceChangeListener.class);
    private Map<IDevice, AndroidDevice> connectedDevices = new HashMap<>();

    public DeviceChangeListener(Map<IDevice, AndroidDevice> connectedDevices) {
        this.connectedDevices = connectedDevices;
    }

    @Override
    public void deviceConnected(IDevice device) {
        logger.info("deviceConnected {}", device.getSerialNumber());
        AndroidDevice ad = new DefaultHardwareDevice(device);
        Iterator<Map.Entry<IDevice, AndroidDevice>> entryIterator = connectedDevices.entrySet().iterator();
        boolean contain = false;
        while (entryIterator.hasNext()) {
            Map.Entry entry = entryIterator.next();
            if (entry.getValue().equals(ad)) {
                contain = true;
                break;
            }
        }
        if (!contain) {
            connectedDevices.put(device, ad);
        }
        sendConnectedGrpc(device.getSerialNumber());
    }

    @Override
    public void deviceDisconnected(IDevice device) {
        logger.info("deviceDisconnected {}", device.getSerialNumber());
        AndroidDevice ad = new DefaultHardwareDevice(device);
        connectedDevices.entrySet().removeIf(entry -> entry.getValue().equals(ad));
        // 汇报web设备断开
        sendDisconnectGrpc(ad.getDevice().getSerialNumber());
        // 本地清楚缓存设备信息
        TestwaScheduled.a_devices.remove(device.getSerialNumber());
    }

    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        logger.debug(device.getSerialNumber() + " " + changeMask);
    }

    private void sendDisconnectGrpc(String deviceId) {
        ApplicationContext context = ApplicationContextUtil.getApplicationContext();
        context.publishEvent(new DeviceDisconnectEvent(this, deviceId));

    }
    private void sendConnectedGrpc(String deviceId) {
        ApplicationContext context = ApplicationContextUtil.getApplicationContext();
        context.publishEvent(new DeviceConnectedEvent(this, deviceId));

    }

}