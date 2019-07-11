package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

/**
 * issue 的处理负责人
 *
 * @author wen
 * @create 2019-01-07 11:18
 */
@Data
@Table(name="dis_issue_assignee")
public class IssueAssignee extends BaseEntity {

    // issue
    @Column(name = "issue_id")
    private Long issueId;
    // 负责人
    @Column(name = "assignee_id")
    private Long assigneeId;
    // 分配角色，暂时还不用
    @Column(name = "assign_role")
    private DB.IssueAssignRoleEnum assignRole;
    // 创建时间
    @Column(name = "create_time")
    private Date createTime;



}
