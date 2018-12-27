package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 项目用户邀请
 * Created by wen on 29/07/2017.
 */
@Data
@Table(name="project_invite")
public class ProjectInvite extends BaseEntity {

    @Column(name = "projectId")
    private Long projectId;
    @Column(name = "inviteBy")
    private Long inviteBy;
    @Column(name = "email")
    private String email;
    @Column(name = "username")
    private String username;
    @Column(name = "sendTime")
    private Date sendTime;
    @Column(name = "status")
    private int status;  // 接收状态

    @JsonIgnore
    @Column(name = "lock_version")
    private Long lockVersion;

}
