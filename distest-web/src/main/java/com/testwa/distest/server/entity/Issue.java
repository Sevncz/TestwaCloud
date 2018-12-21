package com.testwa.distest.server.entity;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

@Data
@Table(name="dis_issue")
public class Issue extends BaseEntity {

    // 所属项目
    @Column(name = "project_id")
    private Long projectId;
    // 负责人
    @Column(name = "assignee_id")
    private Long assigneeId;
    // 创建人
    @Column(name = "author_id")
    private Long authorId;
    // 创建时间
    @Column(name = "create_time")
    private Date createTime;
    // 标题
    @Column(name = "title")
    private String title;
    // 状态
    @Column(name = "state")
    private DB.IssueStateEnum state;

}
