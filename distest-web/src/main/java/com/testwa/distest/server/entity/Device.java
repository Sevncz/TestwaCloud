package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 26/10/2017.
 */
@Data
@NoArgsConstructor
@TableName("device")
public class Device extends BaseEntity {

    private String deviceId;
    private DB.PhoneOnlineStatus onlineStatus;
    private DB.DeviceWorkStatus workStatus;
    private DB.DeviceDebugStatus debugStatus;
    private DB.PhoneOS phoneOS;
    /**注册时间**/
    private Date createTime = new Date();
    /**更新时间**/
    private Date updateTime = new Date();

    private Long lastUserId;
    @JsonIgnore
    private String lastUserToken;

    private String cpuabi; // android:armeabi-v7a   iphone: arm64
    private String sdk; // 23
    private String width;
    private String height;
    private String osName; // 设备系统 ANDROID23(6.0)    iphone: iPhone OS
    private String density; // 密度
    private String osVersion; // 系统版本 android: 6.0.1    iphone: 11.3
    private String host; // vpba27.mtv.corp.google.com
    private String model; // 型号  Nexus 6
    private String brand; // 品牌 google

    private Boolean stfagentInstall;
    private Boolean appiumserverInstall;
    private Boolean appiumserverdebugInstall;
    private Boolean keyboardserviceInstall;

    private Boolean settingsInstall;
    private Boolean unlockInstall;
    private Boolean unicodeIMEInstall;
    private Boolean selendroidInstall;

}
