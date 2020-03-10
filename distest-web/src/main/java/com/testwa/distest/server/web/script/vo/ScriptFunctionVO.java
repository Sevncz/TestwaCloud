package com.testwa.distest.server.web.script.vo;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.distest.server.entity.ScriptAction;
import lombok.Data;

import java.util.List;

@Data
public class ScriptFunctionVO {

    private String scriptCaseId;
    private String functionId;
    private String title;
    private String feature;
    private String testcaseLink;
    private String issueLink;
    private String severity;
    private String args;
    private String parameter;
    private List<ScriptActionVO> actions;

}
