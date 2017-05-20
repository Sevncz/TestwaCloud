package com.testwa.distest.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/11.
 */
@Document(collection = "t_report")
public class TestwaReport {

    @Id
    private String id;
    @Indexed
    private String testcaseId;
    private String testcaseName;
    private List<String> scripts;

    private String appId;
    private String a_name;
    private String a_version;
    private String a_packageName;
    private String a_activity;
    private String a_sdkVersion;
    private String a_targetSdkVersion;

    private List<String> devices;
    private String projectId;
    private String projectName;
    // 案例拥有者
    private String userId;
    private String userName;
    // 执行者
    private String excuteUserId;
    private String excuteUserName;

    private Map<String, String> errorInfo = new HashMap<>();

    private Boolean disable;
    private Date modifyDate;

    public TestwaReport() {
    }

    public TestwaReport(TestwaTestcase testcase, TestwaApp app, List<String> devices, User currentUser) {
        this.testcaseId = testcase.getId();
        this.testcaseName = testcase.getName();
        this.scripts = testcase.getScripts();
        this.appId = testcase.getAppId();
        this.a_name = app.getName();
        this.a_packageName = app.getPackageName();
        this.a_activity = app.getActivity();
        this.a_sdkVersion = app.getSdkVersion();
        this.a_targetSdkVersion = app.getTargetSdkVersion();
        this.a_version = app.getVersion();
        this.devices = devices;
        this.projectId = testcase.getProjectId();
        this.userId = testcase.getUserId();
        this.userName = testcase.getUserName();

        this.excuteUserId = currentUser.getId();
        this.excuteUserName = currentUser.getUsername();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(String testcaseId) {
        this.testcaseId = testcaseId;
    }

    public String getTestcaseName() {
        return testcaseName;
    }

    public void setTestcaseName(String testcaseName) {
        this.testcaseName = testcaseName;
    }

    public List<String> getScripts() {
        return scripts;
    }

    public void setScripts(List<String> scripts) {
        this.scripts = scripts;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
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

    public Map<String, String> getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(Map<String, String> errorInfo) {
        this.errorInfo = errorInfo;
    }

    public void putErrorInfo(String type, String message){
        this.errorInfo.put(type, message);
    }

    public String getA_name() {
        return a_name;
    }

    public void setA_name(String a_name) {
        this.a_name = a_name;
    }

    public String getA_packageName() {
        return a_packageName;
    }

    public void setA_packageName(String a_packageName) {
        this.a_packageName = a_packageName;
    }

    public String getA_activity() {
        return a_activity;
    }

    public void setA_activity(String a_activity) {
        this.a_activity = a_activity;
    }

    public String getA_sdkVersion() {
        return a_sdkVersion;
    }

    public void setA_sdkVersion(String a_sdkVersion) {
        this.a_sdkVersion = a_sdkVersion;
    }

    public String getA_targetSdkVersion() {
        return a_targetSdkVersion;
    }

    public void setA_targetSdkVersion(String a_targetSdkVersion) {
        this.a_targetSdkVersion = a_targetSdkVersion;
    }

    public String getA_version() {
        return a_version;
    }

    public void setA_version(String a_version) {
        this.a_version = a_version;
    }

    public Boolean getDisable() {
        return disable;
    }

    public void setDisable(Boolean disable) {
        this.disable = disable;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getExcuteUserId() {
        return excuteUserId;
    }

    public void setExcuteUserId(String excuteUserId) {
        this.excuteUserId = excuteUserId;
    }

    public String getExcuteUserName() {
        return excuteUserName;
    }

    public void setExcuteUserName(String excuteUserName) {
        this.excuteUserName = excuteUserName;
    }
}
