package com.testwa.core.cmd;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Created by wen on 22/08/2017.
 */
@ToString
public class RemoteRunCommand {

    private Long taskCode;
    private AppInfo appInfo;
    private String deviceId;
    private List<RemoteTestcaseContent> testcaseList;
    private Boolean install = true;  // 默认安装
    private int cmd;  // 0 关闭，1 启动

    public Long getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(Long taskCode) {
        this.taskCode = taskCode;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<RemoteTestcaseContent> getTestcaseList() {
        return testcaseList;
    }

    public void setTestcaseList(List<RemoteTestcaseContent> testcaseList) {
        this.testcaseList = testcaseList;
    }

    public Boolean getInstall() {
        return install;
    }

    public void setInstall(Boolean install) {
        this.install = install;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }
}
