package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by wen on 26/10/2017.
 */
@Data
@NoArgsConstructor
@Table(name="device")
public class Device extends BaseEntity {

    @Column(name = "deviceId")
    private String deviceId;
    @Column(name = "onlineStatus")
    private DB.PhoneOnlineStatus onlineStatus;
    @Column(name = "workStatus")
    private DB.DeviceWorkStatus workStatus;
    @Column(name = "debugStatus")
    private DB.DeviceDebugStatus debugStatus;
    @Column(name = "phoneOS")
    private DB.PhoneOS phoneOS;
    /**注册时间**/
    @Column(name = "createTime")
    private Date createTime = new Date();
    /**更新时间**/
    @Column(name = "updateTime")
    private Date updateTime = new Date();

    @Column(name = "lastUserId")
    private Long lastUserId;
    @JsonIgnore
    @Column(name = "lastUserToken")
    private String lastUserToken;

    @Column(name = "cpuabi")
    private String cpuabi; // android:armeabi-v7a   iphone: arm64
    @Column(name = "sdk")
    private String sdk; // 23
    @Column(name = "width")
    private String width;
    @Column(name = "height")
    private String height;
    @Column(name = "osName")
    private String osName; // 设备系统 ANDROID23(6.0)    iphone: iPhone OS
    @Column(name = "density")
    private String density; // 密度
    @Column(name = "osVersion")
    private String osVersion; // 系统版本 android: 6.0.1    iphone: 11.3
    @Column(name = "host")
    private String host; // vpba27.mtv.corp.google.com
    @Column(name = "model")
    private String model; // 型号  Nexus 6
    @Column(name = "brand")
    private String brand; // 品牌 google

    @Column(name = "stfagentInstall")
    private Boolean stfagentInstall;
    @Column(name = "appiumserverInstall")
    private Boolean appiumserverInstall;
    @Column(name = "appiumserverdebugInstall")
    private Boolean appiumserverdebugInstall;
    @Column(name = "keyboardserviceInstall")
    private Boolean keyboardserviceInstall;

    @Column(name = "settingsInstall")
    private Boolean settingsInstall;
    @Column(name = "unlockInstall")
    private Boolean unlockInstall;
    @Column(name = "unicodeIMEInstall")
    private Boolean unicodeIMEInstall;
    @Column(name = "selendroidInstall")
    private Boolean selendroidInstall;

}
