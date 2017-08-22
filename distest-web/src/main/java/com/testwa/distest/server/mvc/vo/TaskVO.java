package com.testwa.distest.server.mvc.vo;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.mvc.model.Task;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 21/08/2017.
 */
@Data
public class TaskVO {

    private String id;
    private String appId;
    private String projectId;
    private List<String> testcaseIds;
    private String creator;
    private String createDate;
    private String modifyDate;

    public TaskVO(Task task) {

        this.id = task.getId();
        this.appId = task.getAppId();
        this.projectId = task.getProjectId();
        this.testcaseIds = task.getTestcaseIds();
        this.createDate = TimeUtil.formatTimeStamp(task.getCreateDate().getTime());
    }
}
