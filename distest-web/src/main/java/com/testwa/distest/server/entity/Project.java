package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 项目表
 * Created by wen on 16/8/30.
 */
@Data
@Table(name="project")
public class Project extends BaseEntity {

    @Column(name = "projectName")
    private String projectName;
    @Column(name = "description")
    private String description;
    @Column(name = "createTime")
    private Date createTime;
    @Column(name = "updateTime")
    private Date updateTime;
    @Column(name = "createBy")
    private Long createBy;
    @Column(name = "updateBy")
    private Long updateBy;

    @JsonIgnore
    @Column(name = "lock_version")
    private Long lockVersion;

}
