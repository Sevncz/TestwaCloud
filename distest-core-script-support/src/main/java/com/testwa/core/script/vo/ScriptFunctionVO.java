package com.testwa.core.script.vo;

import lombok.Data;

import java.util.List;

@Data
public class ScriptFunctionVO {

    private String scriptCaseId;
    private String functionId;
    private String title;
    private String feature;
    private String funcDesc;
    private String testcaseLink;
    private String issueLink;
    private String severity;
    private String args;
    private String parameter;
    private List<ScriptActionVO> actions;

}
