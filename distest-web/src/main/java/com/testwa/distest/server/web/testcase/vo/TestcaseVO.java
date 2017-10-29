package com.testwa.distest.server.web.testcase.vo;

import com.testwa.core.common.enums.DB;
import com.testwa.distest.server.web.script.vo.ScriptVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 23/10/2017.
 */
@Data
public class TestcaseVO {

    private Long id;
    private String tag;
    private String caseName;
    private Long projectId;
    private String description;
    private DB.RunMode exeMode;

    private Date createTime;
    private Long createBy;

    private List<ScriptVO> scriptVOs;

}
