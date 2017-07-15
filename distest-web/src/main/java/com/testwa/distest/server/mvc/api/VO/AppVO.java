package com.testwa.distest.server.mvc.api.VO;


import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.mvc.model.App;

/**
 * Created by wen on 2016/11/19.
 */
public class AppVO {

    private String id;
    private String name;
    private String aliasName;
    private String packageName;
    private String activity;
    private String sdkVersion;
    private String targetSdkVersion;
    private String version;
    private String type;
    private String projectId;
    private String project;
    private String md5;
    private String size;
    private String createDate;

    private String userId;
    private String userName;

    public AppVO(App app){
        this.id = app.getId();
        this.name = app.getName();
        this.aliasName = app.getAliasName();
        this.packageName = app.getPackageName();
        this.activity = app.getActivity();
        this.sdkVersion = app.getSdkVersion();
        this.targetSdkVersion = app.getTargetSdkVersion();
        this.version = app.getVersion();
        this.type = app.getType();
        this.projectId = app.getProjectId();
        this.project = app.getProjectName();
        this.userId = app.getUserId();
        this.md5 = app.getMd5();
        this.size = app.getSize();
        if(app.getCreateDate() != null){
            this.createDate = TimeUtil.formatTimeStamp(app.getCreateDate().getTime());
        }
        this.userName = app.getUsername();
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

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public void setTargetSdkVersion(String targetSdkVersion) {
        this.targetSdkVersion = targetSdkVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
