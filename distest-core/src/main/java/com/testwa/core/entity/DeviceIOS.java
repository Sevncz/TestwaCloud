package com.testwa.core.entity;

import com.testwa.core.common.annotation.TableName;
import com.testwa.core.common.enums.DB;
import lombok.Data;

/**
 * Created by wen on 25/10/2017.
 */
@Data
@TableName("device_ios")
public class DeviceIOS extends DeviceBase {

    /**设备标志**/
    private String deviceFlag;
    /**设备编号（和Device主键对应）**/
//    private String deviceId;
    /**以下是MDM推送相关**/
    private  String topic;
    private  String token;
    private  String pushMagic;
    private  String udid;
    private  String unlockToken;
    /**以下是通过MDM获取的**/
    private  String modelName;
    private  String model;
    private  String batteryLevel;
    private  String deviceCapacity;
    private  String availableDeviceCapacity;
    private  String oSVersion;
    /**客户新增的几个参数**/
    private  String serialNumber;
    private  String imei;
    private  String iccid;
    private  String meid;
    private  String isSupervised;
    private  String isDeviceLocatorServiceEnabled;
    private  String isActivationLockEnabled;
    private  String isCloudBackupEnabled;
    private  String wifimac;
    private  String bluetoothMAC;

    /**设备状态（1：已认证；2可控制；-1：已移除）**/
    private String control;

    public DeviceIOS(){
        super(DB.PhoneOS.IOS);
    }

}
