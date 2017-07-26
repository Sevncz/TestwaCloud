package com.testwa.distest.client.model;

import com.testwa.distest.client.rpc.proto.Agent;
import io.rpc.testwa.device.Device;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by wen on 16/9/3.
 */
public class TestwaDevice {
    private String serial; // 设备id

    private String type; // ios or android

    private String status; // offline or online

    private String cpuabi; // armeabi-v7a
    private String sdk; // 23
    private String width;
    private String height;
    private String osName; // 设备系统 ANDROID23(6.0)
    private String density; // 密度
    private String model; // 型号  Nexus 6
    private String brand; // 品牌 google
    private String version; // 系统版本 6.0.1
    private String host; // vpba27.mtv.corp.google.com

    public TestwaDevice() {
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCpuabi() {
        return cpuabi;
    }

    public void setCpuabi(String cpuabi) {
        this.cpuabi = cpuabi;
    }

    public String getSdk() {
        return sdk;
    }

    public void setSdk(String sdk) {
        this.sdk = sdk;
    }

    public String getWidth() {
        if(StringUtils.isBlank(width)){
            return "unknown";
        }
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        if(StringUtils.isBlank(height)){
            return "unknown";
        }
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getOsName() {
        if(StringUtils.isBlank(osName)){
            return "unknown";
        }
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getDensity() {
        if(StringUtils.isBlank(density)){
            return "unknown";
        }
        return density;
    }

    public void setDensity(String density) {
        this.density = density;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Device toAgentDevice(){
        return Device.newBuilder()
                .setDeviceId(this.getSerial())
                .setDensity(this.getDensity())
                .setOsName(this.getOsName())
                .setWidth(this.getWidth())
                .setHeight(this.getHeight())
                .setStatus(Device.LineStatus.valueOf(this.getStatus()))
                .setCpuabi(this.getCpuabi())
                .setSdk(this.getSdk())
                .setHost(this.getHost())
                .setModel(this.getModel())
                .setBrand(this.getBrand())
                .setVersion(this.getVersion())
                .build();
    }
}
