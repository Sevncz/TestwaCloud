package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

/**
 * 设备分享范围
 */
@Data
@Table(name="dis_device_share_scope")
public class DeviceShareScope extends BaseEntity {

    @Column(name = "device_id")
    private String deviceId;
    @Column(name = "share_scope")
    private DB.DeviceShareScopeEnum shareScope;
    @Column(name = "create_by")
    private Long createBy;
    @Column(name = "create_time")
    private Date createTime;



}
