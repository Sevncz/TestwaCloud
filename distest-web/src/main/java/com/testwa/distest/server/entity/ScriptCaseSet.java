package com.testwa.distest.server.entity;

import com.alibaba.fastjson.JSON;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import com.testwa.core.base.mybatis.annotation.Transient;
import com.testwa.distest.server.service.script.service.ScriptCaseService;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wen on 16/9/1.
 */
@Data
@ToString
@Table(name = "dis_script_case_set")
@ApiModel(value = "脚本测试集")
public class ScriptCaseSet extends ProjectBaseEntity {
    @Column(name = "tag")
    private String tag;
    @Column(name = "case_name")
    private String caseName;
    @Column(name = "description")
    private String description;
    @Column(name = "app_info_id")
    private Long appInfoId;
    @Column(name = "package_name")
    private String packageName;
    @Column(name = "app_name")
    private String appName;
    @Column(name = "script_case_ids")
    private String scriptCaseIds;

    @Transient
    private List<TestcaseDetail> testcaseDetails;

    @Transient
    private User createUser;

    @Transient
    private User updateUser;
}