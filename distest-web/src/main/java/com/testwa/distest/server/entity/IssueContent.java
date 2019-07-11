package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.Date;

@Data
@Table(name="dis_issue_content")
public class IssueContent extends BaseEntity {
    // 内容
    @Column(name = "content")
    private String content;
    //  对应的 issue
    @Column(name = "issue_id")
    private Long issueId;



}
