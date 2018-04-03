package com.testwa.distest.server.entity;

import com.testwa.core.base.annotation.Column;
import com.testwa.core.base.annotation.TableName;
import com.testwa.core.base.bo.BaseEntity;
import com.testwa.distest.common.enums.DB;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 12/08/2017.
 */
@Data
@ToString
@TableName("taskscene")
public class TaskScene extends BaseEntity {

    private String sceneName;
    private Long projectId;
    private Long appId;
    private String description;
    private DB.TaskType exeMode;

    private Date createTime;
    private Date updateTime;
    private Long createBy;
    private Long updateBy;

    @Column(value="app", ignore=true)
    private App app;
    @Column(value="createUser", ignore=true)
    private User createUser;
    @Column(value="updateUser", ignore=true)
    private User updateUser;
    @Column(value="taskSceneDetails", ignore=true)
    private List<TaskSceneDetail> taskSceneDetails;

}
