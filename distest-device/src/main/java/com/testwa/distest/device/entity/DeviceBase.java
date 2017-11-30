package com.testwa.distest.device.entity;

import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.device.enums.DB;
import lombok.Data;

import java.util.Date;

/**
 * Created by wen on 26/10/2017.
 */
@Data
@TableName("device_base")
public class DeviceBase extends BaseEntity {

    private String deviceId;
    private DB.PhoneOnlineStatus onlineStatus;
    private DB.PhoneOS phoneOS;
    /**注册时间**/
    private Date createTime = new Date();
    /**更新时间**/
    private Date updateTime = new Date();

    protected DeviceBase(DB.PhoneOS phoneOS) {
        this.phoneOS = phoneOS;
    }

}
