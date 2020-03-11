package com.testwa.core.script.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 09/03/2020.
 */
@Data
public class ScriptCaseVO {

    private String scriptCaseId;
    private String scriptCaseName;
    private String scriptCaseDesc;
    private Date updateTime;
    private Long updateBy;
    private Date createTime;
    private Long createBy;
    private String platform;

    private List<ScriptFunctionVO> functions;

}
