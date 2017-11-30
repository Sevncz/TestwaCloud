package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 谁的设备允许谁使用
 */
@Data
@TableName("device_auth")
public class DeviceAuth extends BaseEntity {

    private String deviceId;
    private Long userId;
    private Long createBy;
    private Date createTime;
    private User createUser;
    private User user;

}
