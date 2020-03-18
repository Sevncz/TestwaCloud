package com.testwa.distest.server.entity;

import com.testwa.core.base.bo.BaseEntity;
import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.core.base.mybatis.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name="dis_script_function")
public class ScriptFunction extends ProjectBase {

    /**
     * 脚本案例ID
     */
    @Column(name = "script_case_id")
    private String scriptCaseId;
    /**
     * 方法uuid
     */
    @Column(name = "function_id")
    private String functionId;
    /**
     * 用例标题
     */
    @Column(name = "title")
    private String title;
    /**
     * 模块名称
     */
    @Column(name = "feature")
    private String feature;
    /**
     * 方法描述
     */
    @Column(name = "func_desc")
    private String funcDesc;
    /**
     * 测试案例链接
     */
    @Column(name = "testcase_link")
    private String testcaseLink;
    /**
     * 模块名称
     */
    @Column(name = "issue_link")
    private String issueLink;
    /**
     * 严重程度
     */
    @Column(name = "severity")
    private String severity;
    /**
     * 变量args
     */
    @Column(name = "args")
    private String args;
    /**
     * 变量值，JSON格式，可配置多参数
     */
    @Column(name = "parameter")
    private String parameter;

}
