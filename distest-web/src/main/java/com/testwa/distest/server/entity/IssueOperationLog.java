package com.testwa.distest.server.entity;

import com.testwa.core.base.bo.BaseEntity;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

/**
 * issue 的
 *
 * @author wen
 * @create 2018-12-29 11:20
 */
@Data
@Table(name="dis_issue_op_log")
public class IssueOperationLog extends BaseEntity {
    @Column(name = "issue_id")
    private Long issueId;
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "op_type")
    private DB.IssueOpTypeEnum opType;
    @Column(name = "content")
    private String content;
}
