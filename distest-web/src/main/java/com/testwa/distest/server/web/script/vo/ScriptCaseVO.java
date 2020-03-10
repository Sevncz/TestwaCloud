package com.testwa.distest.server.web.script.vo;

import com.testwa.core.base.mybatis.annotation.Column;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.ScriptFunction;
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

    private List<ScriptFunctionVO> functions;

}
