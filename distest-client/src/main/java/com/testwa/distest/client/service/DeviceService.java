package com.testwa.distest.client.service;import com.github.cosysoft.device.DeviceStore;import com.github.cosysoft.device.android.AndroidDevice;import com.github.cosysoft.device.android.impl.AndroidDeviceStore;import com.github.cosysoft.device.image.ImageUtils;import com.github.cosysoft.device.model.DeviceInfo;import com.testwa.distest.client.grpc.GrpcClient;import io.grpc.Channel;import org.springframework.beans.BeanUtils;import org.springframework.cache.annotation.Cacheable;import org.springframework.stereotype.Service;import java.awt.image.BufferedImage;import java.util.ArrayList;import java.util.List;@Servicepublic class DeviceService {    private DeviceStore deviceStore = AndroidDeviceStore.getInstance();    @GrpcClient("local-grpc-server")    private Channel serverChannel;    @Cacheable(value = "avatars")    public byte[] getAvatar(String serialId) {        AndroidDevice device = deviceStore.getDeviceBySerial(serialId);        BufferedImage image = device.takeScreenshot();        return ImageUtils.toByteArray(image);    }    public byte[] takeScreenShot(String serialId) {        AndroidDevice device = deviceStore.getDeviceBySerial(serialId);        BufferedImage image = device.takeScreenshot();        return ImageUtils.toByteArray(image);    }    public List<DeviceInfo> getDevices() {        List<DeviceInfo> devices = new ArrayList<>();        for (AndroidDevice device : deviceStore.getDevices()) {            DeviceInfo deviceExtInfo = new DeviceInfo();            DeviceInfo deviceInfo = device.getDeviceInfo();            BeanUtils.copyProperties(deviceInfo, deviceExtInfo);            devices.add(deviceExtInfo);        }        return devices;    }    public DeviceInfo getDeviceBySerial(String serial) {        AndroidDevice d = deviceStore.getDeviceBySerial(serial);        if(d != null){            return d.getDeviceInfo();        }        return null;    }    public AndroidDevice getAndroidDeviceBySerial(String serial) {        return deviceStore.getDeviceBySerial(serial);    }    public String runAdbCommand(String serialId, String cmd) {        AndroidDevice device = deviceStore.getDeviceBySerial(serialId);        return device.runAdbCommand(cmd);    }}