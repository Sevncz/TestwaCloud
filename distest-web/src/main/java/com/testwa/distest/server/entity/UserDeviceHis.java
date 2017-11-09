package com.testwa.distest.server.entity;

import com.testwa.core.common.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;
import java.util.Set;

/**
 * 曾经在用户agent上连接过的设备
 * Created by wen on 2016/12/3.
 */
@Data
public class UserDeviceHis extends BaseEntity {
    private Integer userId;
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

    private Date createDate;
    private Integer scope;

    private Set<String> shareUsers; // 分享给哪些人

    private Boolean disable;
    private Date modifyDate;

    public UserDeviceHis() {
    }

    public UserDeviceHis(Integer userId, DeviceAndroid device) {

        this.userId = userId;
        this.deviceId = device.getDeviceId();
        this.d_brand = device.getBrand();
        this.d_cpuabi = device.getCpuabi();
        this.d_density = device.getDensity();
        this.d_height = device.getHeight();
        this.d_host = device.getHost();
        this.d_model = device.getModel();
        this.d_osName = device.getOsName();
        this.d_sdk = device.getSdk();
//        this.d_status = device.getStatus();
//        this.d_type = device.getType();
        this.d_version = device.getVersion();
        this.d_width = device.getWidth();

        this.disable = false;
        this.createDate = new Date();

        this.scope = DB.ShareScopeEnum.All.getValue();

    }

}
