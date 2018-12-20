package com.testwa.distest.server.entity;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

/**
 * 设备使用分组
 */
@Data
@Table(name="dis_device_sharer")
public class DeviceSharer extends BaseEntity {

    @Column(name = "device_id")
    private String deviceId;
    // 谁分享的
    @Column(name = "from_user_id")
    private Long fromUserId;
    // 分享给谁 - 包括 用户 项目
    @Column(name = "sharer_id")
    private Long sharerId;
    @Column(name = "start_time")
    private Date startTime;
    @Column(name = "end_time")
    private Date endTime;
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "share_scope_type")
    private DB.DeviceShareScopeTypeEnum shareScopeType;


}
