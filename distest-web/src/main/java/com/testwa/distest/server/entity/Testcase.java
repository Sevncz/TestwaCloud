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
public class Testcase extends ProjectBaseEntity {
    private String tag;
    private String caseName;
    private String description;
    private Long appInfoId;
    private String packageName;
    private String appName;

    @Column(value="scripts", ignore=true)
    private List<TestcaseDetail> testcaseDetails;
    @Column(value="createUser", ignore=true)
    private User createUser;
    @Column(value="updateUser", ignore=true)
    private User updateUser;
}
