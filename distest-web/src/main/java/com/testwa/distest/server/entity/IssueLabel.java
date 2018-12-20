package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

@Data
@Table(name="dis_issue_label")
public class IssueLabel extends BaseEntity {
    @JsonIgnore
    @Column(name = "project_id")
    private Long projectId;
    @Column(name = "name")
    private String name;
    @Column(name = "color")
    private String color;
    @JsonIgnore
    @Column(name = "create_by")
    private Long createBy;
}
