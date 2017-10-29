package com.testwa.core.entity;

import com.testwa.core.common.annotation.TableName;
import com.testwa.core.common.bo.BaseEntity;
import com.testwa.core.common.enums.DB;
import lombok.Data;

import java.util.Date;

/**
 * 项目成员表
 * Created by wen on 29/07/2017.
 */
@Data
@TableName("project_member")
public class ProjectMember extends BaseEntity {
    private Long projectId;
    private Long memberId;
    private Long inviteBy;
    private Date createTime;
    private DB.ProjectRole projectRole;
}
