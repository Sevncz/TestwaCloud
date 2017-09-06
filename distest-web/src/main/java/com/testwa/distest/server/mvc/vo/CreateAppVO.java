package com.testwa.distest.server.mvc.vo;

import javax.validation.constraints.NotNull;

/**
 * Created by yxin on 8/8/2017.
 */
public class CreateAppVO {
    @NotNull
    public String id;
    @NotNull
    public String projectId;
    @NotNull
    private String tag;
    private String description;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CreateAppDTO{" +
                "id='" + id + '\'' +
                ", projectId='" + projectId + '\'' +
                '}';
    }
}
