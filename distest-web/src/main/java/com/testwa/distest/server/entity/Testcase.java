package com.testwa.distest.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
@Data
@ToString
@TableName("testcase")
public class Testcase extends BaseEntity {
    private String tag;
    private String caseName;
    private Long projectId;
    private String description;

    private Date createTime;
    private Long createBy;
    private Date updateTime;
    private Long updateBy;
    @JsonIgnore
    private Boolean enabled;

    @Column(value="scripts", ignore=true)
    private List<TestcaseDetail> testcaseDetails;
    @Column(value="createUser", ignore=true)
    private User createUser;
    @Column(value="updateUser", ignore=true)
    private User updateUser;
}
