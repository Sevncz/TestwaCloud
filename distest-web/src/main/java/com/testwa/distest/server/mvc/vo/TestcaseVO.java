package com.testwa.distest.server.mvc.vo;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.mvc.model.App;
import com.testwa.distest.server.mvc.model.Testcase;


/**
 * Created by wen on 2016/11/19.
 */
public class TestcaseVO {

    private String id;
    private String appName;
    private String appVersion;
    private String type;

    private String userName;
    private String createDate;
    private String name;
    private String projectName;

    public TestcaseVO(Testcase testcase, App app) {
        this.id = testcase.getId();
        if(app != null){
            this.appName = app.getName();
            this.appVersion = app.getVersion();
            this.type = app.getType();
        }
        this.userName = testcase.getUserName();
        if(testcase.getCreateDate() != null){
            this.createDate = TimeUtil.formatTimeStamp(testcase.getCreateDate().getTime());
        }
        this.name = testcase.getName();
        this.projectName = testcase.getProjectName();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
