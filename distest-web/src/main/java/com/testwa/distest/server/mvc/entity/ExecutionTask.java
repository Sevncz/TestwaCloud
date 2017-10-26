package com.testwa.distest.server.mvc.entity;

import com.testwa.distest.common.annotation.TableName;
import com.testwa.distest.common.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;

import java.util.*;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@TableName("execution_task")
public class ExecutionTask extends BaseEntity {

    private String taskName;
    private List<DeviceAndroid> devices;
    private Long projectId;
    private App app;
    private Task task;
    private List<Testcase> testcases;
    private Map<String, List<Script>> scripts;
    private DB.TaskStatus status;
    private Long createBy;
    private Date createTime;
    private Date endTime;

}
