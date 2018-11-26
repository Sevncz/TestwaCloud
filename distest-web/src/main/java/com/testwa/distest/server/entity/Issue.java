package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

@Data
@TableName("dis_issue")
public class Issue extends BaseEntity {

    // 所属项目
    @Column(value = "project_id")
    private Long projectId;
    // 标签
    @Column(value = "label_id")
    private Long labelId;
    // 负责人
    @Column(value = "assignee_id")
    private Long assigneeId;
    // 创建人
    @Column(value = "author_id")
    private Long authorId;
    // 创建时间
    @Column(value = "create_time")
    private Date createTime;
    // 标题
    @Column(value = "title")
    private String title;
    // 内容
    @Column(value = "content")
    private String content;
    // 状态
    @Column(value = "state")
    private DB.IssueStateEnum state;

}
