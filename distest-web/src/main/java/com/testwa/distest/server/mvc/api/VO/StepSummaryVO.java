package com.testwa.distest.server.mvc.api.VO;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.mvc.model.*;

/**
 * Created by wen on 2016/9/24.
 */
public class StepSummaryVO {

    private String startTime;
    private Long totalTime;
    private String status;

    private String machineName;
    private String usernmae;

    private String appName;
    private String appPackageName;
    private String appActivity;
    private String appSdkVersion;
    private String appTargetSdkVersion;
    private String appVersion;
    private String appSize;

    private String deviceId;
    private String deviceCpuabi; // armeabi-v7a
    private String deviceSdk; // 23
    private String deviceWidth;
    private String deviceHeight;
    private String deviceOsName; // 设备系统 ANDROID23(6.0)
    private String deviceDensity; // 密度
    private String deviceModel; // 型号  Nexus 6
    private String deviceBrand; // 品牌 google
    private String deviceVersion; // 系统版本 6.0.1
    private String deviceHost; // vpba27.mtv.corp.google.com

    public StepSummaryVO(ReportDetail detail, ReportSdetail sdetail) {
        this.startTime = TimeUtil.formatTimeStamp(sdetail.getStartTime().getTime());
        this.totalTime = sdetail.getTotalTime();
        this.machineName = sdetail.getMachineName();
        this.usernmae = sdetail.getUsername();

        this.appName = detail.getA_name();
        this.appPackageName = detail.getA_packageName();
        this.appActivity = detail.getA_activity();
        this.appSdkVersion = detail.getA_sdkVersion();
        this.appTargetSdkVersion = detail.getA_targetSdkVersion();
        this.deviceId = detail.getDeviceId();
        this.deviceCpuabi = detail.getD_cpuabi();
        this.deviceSdk = detail.getD_sdk();
        this.deviceWidth = detail.getD_width();
        this.deviceHeight = detail.getD_height();
        this.deviceOsName = detail.getD_osName();
        this.deviceDensity = detail.getD_density();
        this.deviceModel = detail.getD_model();
        this.deviceBrand = detail.getD_brand();
        this.deviceVersion = detail.getD_version();
        this.deviceHost = detail.getD_host();

        this.status = sdetail.getStepStatus() > 0 ? "失败" : "成功";
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public String getAppActivity() {
        return appActivity;
    }

    public void setAppActivity(String appActivity) {
        this.appActivity = appActivity;
    }

    public String getAppSdkVersion() {
        return appSdkVersion;
    }

    public void setAppSdkVersion(String appSdkVersion) {
        this.appSdkVersion = appSdkVersion;
    }

    public String getAppTargetSdkVersion() {
        return appTargetSdkVersion;
    }

    public void setAppTargetSdkVersion(String appTargetSdkVersion) {
        this.appTargetSdkVersion = appTargetSdkVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppSize() {
        return appSize;
    }

    public void setAppSize(String appSize) {
        this.appSize = appSize;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceCpuabi() {
        return deviceCpuabi;
    }

    public void setDeviceCpuabi(String deviceCpuabi) {
        this.deviceCpuabi = deviceCpuabi;
    }

    public String getDeviceSdk() {
        return deviceSdk;
    }

    public void setDeviceSdk(String deviceSdk) {
        this.deviceSdk = deviceSdk;
    }

    public String getDeviceWidth() {
        return deviceWidth;
    }

    public void setDeviceWidth(String deviceWidth) {
        this.deviceWidth = deviceWidth;
    }

    public String getDeviceHeight() {
        return deviceHeight;
    }

    public void setDeviceHeight(String deviceHeight) {
        this.deviceHeight = deviceHeight;
    }

    public String getDeviceOsName() {
        return deviceOsName;
    }

    public void setDeviceOsName(String deviceOsName) {
        this.deviceOsName = deviceOsName;
    }

    public String getDeviceDensity() {
        return deviceDensity;
    }

    public void setDeviceDensity(String deviceDensity) {
        this.deviceDensity = deviceDensity;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceBrand() {
        return deviceBrand;
    }

    public void setDeviceBrand(String deviceBrand) {
        this.deviceBrand = deviceBrand;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public String getDeviceHost() {
        return deviceHost;
    }

    public void setDeviceHost(String deviceHost) {
        this.deviceHost = deviceHost;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getUsernmae() {
        return usernmae;
    }

    public void setUsernmae(String usernmae) {
        this.usernmae = usernmae;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
