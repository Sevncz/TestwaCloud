package com.testwa.distest.server.web.device.vo;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.web.auth.vo.UserVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MyAndroidDeviceVO {
    private Long id;
    private String deviceId;
    private DB.PhoneOnlineStatus onlineStatus;
    private DB.PhoneOS phoneOS;
    private String model; // 型号  Nexus 6
    private String brand; // 品牌 google

    private String cpuabi; // armeabi-v7a
    private String sdk; // 23
    private String width;
    private String height;
    private String osName; // 设备系统 ANDROID23(6.0)
    private String density; // 密度
    private String osVersion; // 系统版本 6.0.1
    private String host; // vpba27.mtv.corp.google.com

    private List<UserVO> userList;

}
