package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

/**
 * 设备分享范围
 */
@Data
@TableName("device_share_scope")
public class DeviceShareScope extends BaseEntity {

    @Column(value = "device_id")
    private String deviceId;
    @Column(value = "share_scope")
    private DB.DeviceShareScopeEnum shareScope;
    @Column(value = "create_by")
    private Long createBy;
    @Column(value = "create_time")
    private Date createTime;

}
