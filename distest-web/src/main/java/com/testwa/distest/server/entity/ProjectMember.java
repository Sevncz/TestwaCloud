package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

/**
 * 项目成员表
 * Created by wen on 29/07/2017.
 */
@Data
@Table(name="project_member")
public class ProjectMember extends BaseEntity {
    @Column(name = "projectId")
    private Long projectId;
    @Column(name = "memberId")
    private Long memberId;
    @Column(name = "inviteBy")
    private Long inviteBy;
    @Column(name = "createTime")
    private Date createTime;
    @Column(name = "projectRole")
    private DB.ProjectRole projectRole;

    @JsonIgnore
    @Column(name = "lock_version")
    private Long lockVersion;
}
