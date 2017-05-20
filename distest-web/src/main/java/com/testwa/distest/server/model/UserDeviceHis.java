package com.testwa.distest.server.model;

import com.testwa.distest.server.model.permission.UserShareScope;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 曾经在用户agent上连接过的设备
 * Created by wen on 2016/12/3.
 */
@Document(collection = "t_user_device_his")
public class UserDeviceHis {

    @Id
    private String id;
    private String userId;
    private String deviceId;

    private String d_type; // ios or android

    private String d_status; // offline or online

    private String d_cpuabi; // armeabi-v7a
    private String d_sdk; // 23
    private String d_width;
    private String d_height;
    private String d_osName; // 设备系统 ANDROID23(6.0)
    private String d_density; // 密度
    private String d_model; // 型号  Nexus 6
    private String d_brand; // 品牌 google
    private String d_version; // 系统版本 6.0.1
    private String d_host; // vpba27.mtv.corp.google.com

    @CreatedDate
    private Date createDate;
    private Integer scope;

    private Set<String> shareUsers; // 分享给哪些人

    private Boolean disable;
    private Date modifyDate;

    public UserDeviceHis() {
    }

    public UserDeviceHis(String userId, TestwaDevice device) {

        this.userId = userId;
        this.deviceId = device.getId();
        this.d_brand = device.getBrand();
        this.d_cpuabi = device.getCpuabi();
        this.d_density = device.getDensity();
        this.d_height = device.getHeight();
        this.d_host = device.getHost();
        this.d_model = device.getModel();
        this.d_osName = device.getOsName();
        this.d_sdk = device.getSdk();
        this.d_status = device.getStatus();
        this.d_type = device.getType();
        this.d_version = device.getVersion();
        this.d_width = device.getWidth();

        this.disable = true;
        this.createDate = new Date();

        this.scope = UserShareScope.All.getValue();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getScope() {
        return scope;
    }

    public void setScope(Integer scope) {
        this.scope = scope;
    }

    public Boolean getDisable() {
        return disable;
    }

    public void setDisable(Boolean disable) {
        this.disable = disable;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public Set<String> getShareUsers() {
        if(shareUsers == null){
            return shareUsers = new HashSet<>();
        }
        return shareUsers;
    }

    public void setShareUsers(Set<String> shareUsers) {
        this.shareUsers = shareUsers;
    }

    public String getD_type() {
        return d_type;
    }

    public void setD_type(String d_type) {
        this.d_type = d_type;
    }

    public String getD_status() {
        return d_status;
    }

    public void setD_status(String d_status) {
        this.d_status = d_status;
    }

    public String getD_cpuabi() {
        return d_cpuabi;
    }

    public void setD_cpuabi(String d_cpuabi) {
        this.d_cpuabi = d_cpuabi;
    }

    public String getD_sdk() {
        return d_sdk;
    }

    public void setD_sdk(String d_sdk) {
        this.d_sdk = d_sdk;
    }

    public String getD_width() {
        return d_width;
    }

    public void setD_width(String d_width) {
        this.d_width = d_width;
    }

    public String getD_height() {
        return d_height;
    }

    public void setD_height(String d_height) {
        this.d_height = d_height;
    }

    public String getD_osName() {
        return d_osName;
    }

    public void setD_osName(String d_osName) {
        this.d_osName = d_osName;
    }

    public String getD_density() {
        return d_density;
    }

    public void setD_density(String d_density) {
        this.d_density = d_density;
    }

    public String getD_model() {
        return d_model;
    }

    public void setD_model(String d_model) {
        this.d_model = d_model;
    }

    public String getD_brand() {
        return d_brand;
    }

    public void setD_brand(String d_brand) {
        this.d_brand = d_brand;
    }

    public String getD_version() {
        return d_version;
    }

    public void setD_version(String d_version) {
        this.d_version = d_version;
    }

    public String getD_host() {
        return d_host;
    }

    public void setD_host(String d_host) {
        this.d_host = d_host;
    }
}
