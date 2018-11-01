package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

/**
 * 设备使用分组
 */
@Data
@TableName("device_sharer")
public class DeviceSharer extends BaseEntity {

    @Column(value = "device_id")
    private String deviceId;
    // 谁分享的
    @Column(value = "from_user_id")
    private Long fromUserId;
    // 分享给谁 - 包括 用户 项目
    @Column(value = "sharer_id")
    private Long sharerId;
    @Column(value = "start_time")
    private Date startTime;
    @Column(value = "end_time")
    private Date endTime;
    @Column(value = "create_time")
    private Date createTime;
    @Column(value = "share_scope_type")
    private DB.DeviceShareScopeTypeEnum shareScopeType;


}
