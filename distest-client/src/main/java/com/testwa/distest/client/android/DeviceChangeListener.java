package com.testwa.distest.client.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.android.impl.DefaultHardwareDevice;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.control.client.BaseClient;
import com.testwa.distest.client.control.client.Clients;
import com.testwa.distest.client.control.client.MainSocket;
import com.testwa.distest.client.control.client.RemoteClient;
import com.testwa.distest.client.model.TestwaDevice;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.rpc.proto.Agent;
import io.grpc.testwa.device.Device;
import io.grpc.testwa.device.NoUsedDeviceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

/**
 * 设备监听器
 */
public class DeviceChangeListener implements AndroidDebugBridge.IDeviceChangeListener {

    private static final Logger logger = LoggerFactory
            .getLogger(DeviceChangeListener.class);
    private final Map<IDevice, AndroidDevice> connectedDevices;

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
        testwaDevice.setModel(ad.runAdbCommand("shell getprop ro.product.model"));
        testwaDevice.setBrand(ad.runAdbCommand("shell getprop ro.product.brand"));
        testwaDevice.setVersion(ad.runAdbCommand("shell getprop ro.build.version.release"));
        if("ONLINE".equals(ad.getDevice().getState().name().toUpperCase())){
            testwaDevice.setStatus(Agent.Device.LineStatus.ON.name());
        }else{
            testwaDevice.setStatus(Agent.Device.LineStatus.OFF.name());
        }
        Device message = testwaDevice.toAgentDevice();
        MainSocket.getSocket().emit("device", message.toByteArray());
    }

    @Override
    public void deviceDisconnected(IDevice device) {
        logger.info("deviceDisconnected {}", device.getSerialNumber());
        AndroidDevice ad = new DefaultHardwareDevice(device);
        connectedDevices.entrySet().removeIf(entry -> entry.getValue().equals(ad));
        sendDeviceDisconnectMessage(ad.getDevice().getSerialNumber());
    }

    private void sendDeviceDisconnectMessage(String deviceId) {
        NoUsedDeviceRequest message = NoUsedDeviceRequest.newBuilder()
                .setDeviceId(deviceId)
                .setStatus(NoUsedDeviceRequest.LineStatus.DISCONNECTED)
                .build();
        MainSocket.getSocket().emit("deviceDisconnect", message.toByteArray());
    }

    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        logger.debug(device.getSerialNumber() + " " + changeMask);
    }
}