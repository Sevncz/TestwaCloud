package com.testwa.distest.server.web.VO;

import com.testwa.distest.server.model.ReportDetail;

/**
 * Created by wen on 2016/9/24.
 */
public class ReportDetailVO {

    private String detailId;
    private String deviceId;
    private String cpu; // armeabi-v7a
    private String sdk; // 23
    private String width;
    private String height;
    private String osName; // 设备系统 ANDROID23(6.0)
    private String density; // 密度
    private String model; // 型号  Nexus 6
    private String brand; // 品牌 google
    private String version; // 系统版本 6.0.1
    private String host; // vpba27.mtv.corp.google.com

    private String status; // 1失败， 0成功
    private Long success;
    private Long fail;

    public ReportDetailVO(ReportDetail d) {
        this.detailId = d.getId();
        this.deviceId = d.getDeviceId();
        this.cpu = d.getD_cpuabi();
        this.sdk = d.getD_sdk();
        this.width = d.getD_width();
        this.height = d.getD_height();
        this.osName = d.getD_osName();
        this.density = d.getD_density();
        this.model = d.getD_model();
        this.brand = d.getD_brand();
        this.version = d.getD_version();
        this.host = d.getD_host();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
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

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatus(Long success, Long fail) {
        this.setSuccess(success);
        this.setFail(fail);
        this.setStatus(fail != null && fail > 0 ? "1" : "0");
    }

    public Long getSuccess() {
        return success;
    }

    public void setSuccess(Long success) {
        this.success = success;
    }

    public Long getFail() {
        return fail;
    }

    public void setFail(Long fail) {
        this.fail = fail;
    }
}
