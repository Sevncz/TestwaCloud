package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import lombok.Data;

@Data
@TableName("dis_issue_label")
public class IssueLabel extends BaseEntity {
    @Column(value = "project_id")
    private Long projectId;
    @Column(value = "name")
    private String name;
    @Column(value = "color")
    private String color;
    @Column(value = "create_by")
    private Long createBy;
}
