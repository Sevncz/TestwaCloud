package com.testwa.core.entity;

import com.testwa.core.common.annotation.TableName;
import com.testwa.core.common.bo.BaseEntity;
import com.testwa.core.common.enums.DB;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@TableName("task")
public class Task extends BaseEntity {

    private List<DeviceAndroid> devices;
    private DB.TaskStatus status;
    private Long taskSceneId;
    private Long projectId;
    private App app;
    private Long createBy;
    private Date createTime;
    private Date endTime;
    private String errorMsg;

}
