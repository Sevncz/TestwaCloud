package com.testwa.distest.server.entity;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.mybatis.annotation.Transient;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
@Data
@ToString
@Table(name="testcase")
public class Testcase extends ProjectBaseEntity {
    @Column(name = "tag")
    private String tag;
    @Column(name = "caseName")
    private String caseName;
    @Column(name = "description")
    private String description;
    @Column(name = "appInfoId")
    private Long appInfoId;
    @Column(name = "packageName")
    private String packageName;
    @Column(name = "appName")
    private String appName;

    @Transient
    private List<TestcaseDetail> testcaseDetails;
    @Transient
    private User createUser;
    @Transient
    private User updateUser;
}
