package com.testwa.distest.server.mvc.vo;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.mvc.model.Project;

import java.util.List;

/**
 * Created by wen on 2016/11/19.
 */
public class ProjectVO {
    private String id;
    private String name;
    private String createDate;
    private String userId;
    private String userName;
    private List<String> members;
    private String description;

    public ProjectVO(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.userId = project.getUserId();
        this.userName = project.getUserName();
        this.members = project.getMembers();
        this.description = project.getDescription();
        if(project.getCreateDate() != null){
            this.createDate = TimeUtil.formatTimeStamp(project.getCreateDate().getTime());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
