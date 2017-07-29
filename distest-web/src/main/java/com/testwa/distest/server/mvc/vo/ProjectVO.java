package com.testwa.distest.server.mvc.vo;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.mvc.model.Project;

import java.util.List;

/**
 * Created by wen on 2016/11/19.
 */
public class ProjectVO {
    private String id;
    private String projectName;
    private String createTime;
    private String owerId;
    private String owername;
    private String description;

    public ProjectVO(Project project) {
        this.id = project.getId();
        this.projectName = project.getProjectName();
        this.owerId = project.getUserId();
        this.owername = project.getUsername();
        this.description = project.getDescription();
        if(project.getCreateTime() != null){
            this.createTime = TimeUtil.formatTimeStamp(project.getCreateTime());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getOwerId() {
        return owerId;
    }

    public void setOwerId(String owerId) {
        this.owerId = owerId;
    }

    public String getOwername() {
        return owername;
    }

    public void setOwername(String owername) {
        this.owername = owername;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
