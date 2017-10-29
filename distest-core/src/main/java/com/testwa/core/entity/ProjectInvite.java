package com.testwa.core.entity;

import com.testwa.core.common.annotation.TableName;
import com.testwa.core.common.bo.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 项目用户邀请
 * Created by wen on 29/07/2017.
 */
@Data
@TableName("project_invite")
public class ProjectInvite extends BaseEntity {

    private Long projectId;
    private Long inviteBy;
    private String email;
    private String username;
    private Date sendTime;
    private int status;  // 接收状态

}
