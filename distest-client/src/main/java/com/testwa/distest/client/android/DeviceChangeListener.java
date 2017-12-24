package com.testwa.distest.client.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.android.impl.DefaultHardwareDevice;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.control.client.BaseClient;
import com.testwa.distest.client.control.client.MainSocket;
import com.testwa.distest.client.control.client.grpc.GClient;
import com.testwa.distest.client.control.client.grpc.pool.GClientPool;
import com.testwa.distest.client.model.TestwaDevice;
import com.testwa.distest.client.task.TestwaScheduled;
import io.rpc.testwa.device.Device;
import io.rpc.testwa.device.NoUsedDeviceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 设备监听器
 */
public class DeviceChangeListener implements AndroidDebugBridge.IDeviceChangeListener {

    private static final Logger logger = LoggerFactory
            .getLogger(DeviceChangeListener.class);
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
            BaseClient.startRemoteClient(device);
        }
    }

    private void sendDeviceMessageToServer(AndroidDevice ad) {
        TestwaDevice testwaDevice = new TestwaDevice();
        testwaDevice.setSerial(ad.getSerialNumber());
        testwaDevice.setBrand(ad.runAdbCommand("shell getprop ro.product.brand"));
        testwaDevice.setCpuabi(ad.runAdbCommand("shell getprop ro.product.cpu.abi"));
        testwaDevice.setDensity(ad.getDeviceInfo().getDensity().toString());
        testwaDevice.setOsName(ad.getDeviceInfo().getOsName());
        testwaDevice.setWidth(String.valueOf(ad.getScreenSize().getWidth()));
        testwaDevice.setHeight(String.valueOf(ad.getScreenSize().getHeight()));
        testwaDevice.setCpuabi(ad.runAdbCommand("shell getprop ro.product.cpu.abi"));
        testwaDevice.setSdk(ad.runAdbCommand("shell getprop ro.build.version.sdk"));
        testwaDevice.setHost(ad.runAdbCommand("shell getprop ro.build.host"));
        testwaDevice.setModel(ad.runAdbCommand("shell getprop ro.product.dto"));
        testwaDevice.setBrand(ad.runAdbCommand("shell getprop ro.product.brand"));
        testwaDevice.setVersion(ad.runAdbCommand("shell getprop ro.build.version.release"));
        if("ONLINE".equals(ad.getDevice().getState().name().toUpperCase())){
            testwaDevice.setStatus("ON");
        }else{
            testwaDevice.setStatus("OFF");
        }
        Device message = testwaDevice.toAgentDevice();
        MainSocket.getSocket().emit("device", message.toByteArray());
    }

    @Override
    public void deviceDisconnected(IDevice device) {
        logger.info("deviceDisconnected {}", device.getSerialNumber());
        AndroidDevice ad = new DefaultHardwareDevice(device);
        connectedDevices.entrySet().removeIf(entry -> entry.getValue().equals(ad));
//        sendDeviceDisconnectMessage(ad.getDevice().getSerialNumber());
        // 汇报web设备断开
        sendDisconnectGrpc(ad.getDevice().getSerialNumber());
        // 本地清楚缓存设备信息
        TestwaScheduled.a_devices.remove(device.getSerialNumber());
    }

    private void sendDisconnectGrpc(String deviceId) {
        NoUsedDeviceRequest disconnectDevice = NoUsedDeviceRequest.newBuilder()
                .setDeviceId(deviceId)
                .build();
        GClientPool gClientPool = ApplicationContextUtil.getGClientBean();

        GClient c = gClientPool.getClient();
        c.deviceService().disconnect(disconnectDevice);
        gClientPool.release(c);
    }

    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        logger.debug(device.getSerialNumber() + " " + changeMask);
    }
}