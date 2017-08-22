package com.testwa.distest.server.mvc.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by wen on 16/9/11.
 */
@Document(collection = "t_report_detail")
public class ReportDetail {
    @Id
    private String id;

    // testcase info
    @Indexed
    private String testcaseId;
    @Indexed
    private String reportId;

    private List<String> scripts;

    // app info
    @Indexed
    private String appId;
    private String a_name;
    private String a_packageName;
    private String a_activity;
    private String a_sdkVersion;
    private String a_targetSdkVersion;

    // device info
    @Indexed
    private String deviceId;
    private String d_cpuabi; // armeabi-v7a
    private String d_sdk; // 23
    private String d_width;
    private String d_height;
    private String d_osName; // 设备系统 ANDROID23(6.0)
    private String d_density; // 密度
    private String d_model; // 型号  Nexus 6
    private String d_brand; // 品牌 google
    private String d_version; // 系统版本 6.0.1
    private String d_host; // vpba27.mtv.corp.google.com

    // user info
    private String userId;
    // project info
    private String projectId;

    private Boolean disable = false;


    public ReportDetail() {
    }

    public ReportDetail(Report report, App app, TDevice device, String userId) {
        this.reportId = report.getId();
        this.scripts = report.getScripts();
        this.testcaseId = report.getTestcaseId();
        this.appId = app.getId();
        this.a_name = app.getName();
        this.a_packageName = app.getPackageName();
        this.a_activity = app.getActivity();
        this.a_sdkVersion = app.getSdkVersion();
        this.a_targetSdkVersion = app.getTargetSdkVersion();
        this.deviceId = device.getId();
        this.d_cpuabi = device.getCpuabi();
        this.d_sdk = device.getSdk();
        this.d_width = device.getWidth();
        this.d_height = device.getHeight();
        this.d_osName = device.getOsName();
        this.d_density = device.getDensity();
        this.d_model = device.getModel();
        this.d_brand = device.getBrand();
        this.d_version = device.getVersion();
        this.d_host = device.getHost();
        this.userId = userId;
        this.projectId = app.getProjectId();
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getD_cpuabi() {
        return d_cpuabi;
    }

    public void setD_cpuabi(String d_cpuabi) {
        this.d_cpuabi = d_cpuabi;
    }

    public String getD_sdk() {
        return d_sdk;
    }

    public void setD_sdk(String d_sdk) {
        this.d_sdk = d_sdk;
    }

    public String getD_width() {
        return d_width;
    }

    public void setD_width(String d_width) {
        this.d_width = d_width;
    }

    public String getD_height() {
        return d_height;
    }

    public void setD_height(String d_height) {
        this.d_height = d_height;
    }

    public String getD_osName() {
        return d_osName;
    }

    public void setD_osName(String d_osName) {
        this.d_osName = d_osName;
    }

    public String getD_density() {
        return d_density;
    }

    public void setD_density(String d_density) {
        this.d_density = d_density;
    }

    public String getD_model() {
        return d_model;
    }

    public void setD_model(String d_model) {
        this.d_model = d_model;
    }

    public String getD_brand() {
        return d_brand;
    }

    public void setD_brand(String d_brand) {
        this.d_brand = d_brand;
    }

    public String getD_version() {
        return d_version;
    }

    public void setD_version(String d_version) {
        this.d_version = d_version;
    }

    public String getD_host() {
        return d_host;
    }

    public void setD_host(String d_host) {
        this.d_host = d_host;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public Boolean getDisable() {
        return disable;
    }

    public void setDisable(Boolean disable) {
        this.disable = disable;
    }
}
