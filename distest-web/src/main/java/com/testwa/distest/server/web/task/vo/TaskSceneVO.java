package com.testwa.distest.server.web.task.vo;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.web.app.vo.AppVO;
import com.testwa.distest.server.web.testcase.vo.TestcaseVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 21/08/2017.
 */
@Data
public class TaskSceneVO {

    private Long id;
    private String taskName;
    private Long projectId;
    private String description;
    private DB.RunMode exeMode;

    private Date createTime;
    private Date updateTime;
    private Long createBy;
    private Long updateBy;

    private AppVO app;
    private List<TestcaseVO> testcases;

}
