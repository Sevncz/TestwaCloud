package com.testwa.distest.server.mvc.entity;

import com.testwa.distest.common.annotation.TableName;
import com.testwa.distest.common.bo.BaseEntity;
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
    private String invitee;
    private Date sendTime;
    private int status;  // 接收状态

}
