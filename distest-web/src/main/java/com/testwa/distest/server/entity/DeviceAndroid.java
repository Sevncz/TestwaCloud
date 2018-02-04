package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.TableName;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

/**
 * Created by wen on 16/9/3.
 */
@Data
@TableName("device")
public class DeviceAndroid extends Device {

    private String cpuabi; // armeabi-v7a
    private String sdk; // 23
    private String width;
    private String height;
    private String osName; // 设备系统 ANDROID23(6.0)
    private String density; // 密度
    private String osVersion; // 系统版本 6.0.1
    private String host; // vpba27.mtv.corp.google.com

    public DeviceAndroid(){
        super(DB.PhoneOS.ANDROID);
    }

}