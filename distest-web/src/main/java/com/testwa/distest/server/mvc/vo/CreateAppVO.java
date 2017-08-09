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

    @Override
    public String toString() {
        return "CreateAppDTO{" +
                "id='" + id + '\'' +
                ", projectId='" + projectId + '\'' +
                '}';
    }
}
