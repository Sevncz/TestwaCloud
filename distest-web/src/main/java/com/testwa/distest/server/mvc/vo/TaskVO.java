package com.testwa.distest.server.mvc.vo;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.mvc.model.Task;
import lombok.Data;

import java.util.List;

/**
 * Created by wen on 21/08/2017.
 */
@Data
public class TaskVO {

    private String id;
    private AppVO app;
    private String name;
    private String projectId;
    private String creator;
    private String createDate;
    private String modifyDate;
    private List<TestcaseVO> testcaseVOs;

    public TaskVO() {
    }

    public TaskVO(Task task) {

        this.id = task.getId();
        this.projectId = task.getProjectId();
        this.creator = task.getCreator();
        this.name = task.getName();
        if(task.getCreateDate() != null){
            this.createDate = TimeUtil.formatTimeStamp(task.getCreateDate());
        }
        if(task.getModifyDate() != null){
            this.modifyDate = TimeUtil.formatTimeStamp(task.getModifyDate());

        }
    }
}
