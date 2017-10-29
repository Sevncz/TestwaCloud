package com.testwa.core.entity;

import com.testwa.core.common.annotation.TableName;
import com.testwa.core.common.bo.BaseEntity;
import com.testwa.core.common.enums.DB;
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
    private Agent agent;

    protected DeviceBase(DB.PhoneOS phoneOS) {
        this.phoneOS = phoneOS;
    }

}
