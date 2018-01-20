package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.TableName;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

/**
 * Created by wen on 25/10/2017.
 */
@Data
@TableName("device")
public class DeviceIOS extends Device {

    /**设备标志**/
    private String deviceFlag;

    private  String batteryLevel;
    private  String deviceCapacity;
    private  String availableDeviceCapacity;
    private  String oSVersion;

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

    public DeviceIOS(){
        super(DB.PhoneOS.IOS);
    }

}
