package com.testwa.distest.server.entity;import com.fasterxml.jackson.annotation.JsonIgnore;import com.testwa.core.base.bo.BaseEntity;import lombok.Data;import java.util.Date;@Datapublic class ProjectBaseEntity extends BaseEntity {    private Long projectId;    @JsonIgnore    private Boolean enabled;    private Date createTime;    private Date updateTime;    private Long createBy;    private Long updateBy;}