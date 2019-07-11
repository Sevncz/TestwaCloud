package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import lombok.Data;

import java.util.Date;

/**
 * issue 评论
 *
 * @author wen
 * @create 2018-12-26 14:56
 */
@Data
@Table(name="dis_issue_comment")
public class IssueComment extends BaseEntity {

    @JsonIgnore
    @Column(name = "issue_id")
    private Long issueId;
    // 内容
    @Column(name = "content")
    private String content;
    // 创建人
    @Column(name = "author_id")
    private Long authorId;
    // 创建时间
    @Column(name = "create_time")
    private Date createTime;



}
