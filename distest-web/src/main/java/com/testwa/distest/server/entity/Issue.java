package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    // 项目中 issue 的序号
    @Column(name = "issue_seq")
    private Long issueSeq;
    // 创建人
    @Column(name = "author_id")
    private Long authorId;
    // 创建时间
    @Column(name = "create_time")
    private Date createTime;
    // 修改时间
    @Column(name = "update_time")
    private Date updateTime;
    // 标题
    @Column(name = "title")
    private String title;
    // 状态
    @Column(name = "state")
    private DB.IssueStateEnum state;
    // 优先级
    @Column(name = "priority")
    private DB.IssuePriorityEnum priority;
    @JsonIgnore
    @Column(name = "lock_version")
    private Long lockVersion;
    @Column(name = "comment_num")
    private Long commentNum;
}
