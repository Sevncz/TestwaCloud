package com.testwa.core.script;

import lombok.Data;

import java.util.List;

@Data
public class Function {

    /**
     * 脚本ID
     */
    private Long scriptv2Id;
    /**
     * 方法内代码
     */
    private List<String> actions;
    /**
     * 用例标题
     */
    private String title;
    /**
     * 模块名称
     */
    private String feature;
    /**
     * 测试案例链接
     */
    private String testcaseLink;
    /**
     * 模块名称
     */
    private String issueLink;
    /**
     * 严重程度
     */
    private String severity;
    /**
     * 变量key
     */
    private String key;
    /**
     * 变量值JSON，可配置多参数
     */
    private String valueJson;

}
