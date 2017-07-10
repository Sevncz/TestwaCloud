package com.testwa.distest.server.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
@Document(collection = "t_testcase_logger")
public class TestcaseLogger {
    @Id
    private String id;
    private String caseId;
    private List<String> scripts; // 把这些脚本, 应用, 设备信息都冗余的保存在这里
    private String appId; // 引用是没有实体的,不会去主动获取
    private List<String> devices;
    private String userId;
    @CreatedDate
    private Date createDate;
    private Integer looptime; // 循环次数
    private Boolean disable = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getLooptime() {
        return looptime;
    }

    public void setLooptime(Integer looptime) {
        this.looptime = looptime;
    }

    public Boolean getDisable() {
        return disable;
    }

    public void setDisable(Boolean disable) {
        this.disable = disable;
    }
}
