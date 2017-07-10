package com.testwa.distest.server.web.VO;

import com.testwa.distest.server.model.Agent;
import com.testwa.distest.server.model.TDevice;

/**
 * Created by wen on 07/01/2017.
 */
public class DeviceOwnerTableVO {
    private String id;

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
    private String sessionId;
    private Agent agent;

    public DeviceOwnerTableVO(TDevice device) {
        this.id = device.getId();
        this.status = device.getStatus();
        this.cpuabi = device.getCpuabi();
        this.sdk = device.getSdk();
        this.width = device.getWidth();
        this.height = device.getHeight();
        this.osName = device.getOsName();
        this.density = device.getDensity();
        this.model = device.getModel();
        this.brand = device.getBrand();
        this.version = device.getVersion();
        this.host = device.getHost();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getDensity() {
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }
}
