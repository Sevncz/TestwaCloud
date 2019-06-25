package com.testwa.distest.client.device.manager;import com.alibaba.fastjson.JSON;import com.testwa.core.utils.Identities;import com.testwa.distest.client.device.driver.IDeviceRemoteControlDriver;import com.testwa.distest.client.device.driver.IDeviceRemoteControlDriverCapabilities;import com.testwa.distest.client.device.driver.IDeviceRemoteDriverFactory;import io.rpc.testwa.device.DeviceType;import lombok.Data;import lombok.extern.slf4j.Slf4j;import java.util.HashMap;import java.util.Map;import java.util.Objects;import java.util.concurrent.TimeUnit;@Slf4j@Datapublic class DeviceManager {    private final String id = Identities.uuid();    private IDeviceRemoteControlDriverCapabilities capabilities;    private final String host;    private final Integer port;    private final String resourcePath;    private boolean isWaitting = false;    // 设备ID    private String deviceId;    private IDeviceRemoteControlDriver deviceDriver;    public DeviceManager(String host, int port, String resourcePath) {        this.host = host;        this.port = port;        this.resourcePath = resourcePath;    }    public void init(String deviceId, DeviceType deviceType) throws DeviceInitException {        this.capabilities = new IDeviceRemoteControlDriverCapabilities();        this.capabilities.setHost(host);        this.capabilities.setPort(String.valueOf(port));        this.capabilities.setResourcePath(resourcePath);        this.capabilities.setDeviceId(deviceId);        this.deviceId = deviceId;        if(DeviceType.ANDROID.equals(deviceType)) {            this.deviceDriver = IDeviceRemoteDriverFactory.createAndroidDriver(this.capabilities);        }        if(DeviceType.IOS.equals(deviceType)) {            this.deviceDriver = IDeviceRemoteDriverFactory.createIOSDriver(this.capabilities);        }        try {            this.deviceDriver.deviceInit();        } catch (Exception e) {            throw new DeviceInitException("设备["+deviceId+"]初始化失败，等待下次");        }    }    public void reset() {        log.info("[Device Manager Reset] {}", this.deviceDriver.getDeviceId());        // 停止所有服务        this.deviceDriver.stopScreen();        this.deviceDriver.stopLog();        this.deviceDriver.stopRecorder();    }    public void destory() {        // 停止所有服务        this.deviceDriver.stopScreen();        this.deviceDriver.stopLog();        this.deviceDriver.stopRecorder();    }    public boolean deviceIsOnline() {        return deviceDriver.isOnline();    }    public boolean deviceIsRealOffline() {        return deviceDriver.isRealOffline();    }    public void deviceStart() {    }    @Override    public boolean equals(Object o) {        if (this == o){            return true;        }        if (!(o instanceof DeviceManager)){            return false;        }        DeviceManager that = (DeviceManager) o;        if (!Objects.equals(id, that.id)){            return false;        }        return true;    }    @Override    public int hashCode() {        return id.hashCode();    }}