package com.testwa.distest.server.model;

import io.grpc.testwa.testcase.RunningLogRequest;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by wen on 16/8/27.
 */
@Document(collection = "t_procedure_info")
public class TestwaProcedureInfo {
    @Id
    private String id;
    private Integer status;
    private String value;
    private Integer runtime;
    private String cpurate;
    private String memory;
    @Indexed
    private String sessionId;
    @Indexed
    private String deviceId;
    private String reportDetailId;
    private String scriptId;
    private String screenshotPath;
    private String description;
    private String params;
    private String action;
    @CreatedDate
    private Date createDate;
    @Indexed
    private Long timestamp;

    private String userId;

    private String logcatFile;
    private Boolean disable = true;
    private Date modifyDate;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public String getCpurate() {
        return cpurate;
    }

    public void setCpurate(String cpurate) {
        this.cpurate = cpurate;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getReportDetailId() {
        return reportDetailId;
    }

    public void setReportDetailId(String reportDetailId) {
        this.reportDetailId = reportDetailId;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLogcatFile() {
        return logcatFile;
    }

    public void setLogcatFile(String logcatFile) {
        this.logcatFile = logcatFile;
    }

    public void toEntity(RunningLogRequest reqeust) {
        this.action = reqeust.getActionBytes().toStringUtf8();
        this.cpurate = reqeust.getCpurate();
        this.description = reqeust.getDescription();
        this.deviceId = reqeust.getDeviceId();
        this.logcatFile = reqeust.getLogcatFile();
        this.memory = reqeust.getMemory();
        this.params = reqeust.getParams();
        this.reportDetailId = reqeust.getReportDetailId();
        this.runtime = reqeust.getRuntime();
        this.screenshotPath = reqeust.getScreenshotPath();
        this.sessionId = reqeust.getSessionId();
        this.scriptId = reqeust.getScriptId();
        this.status = reqeust.getStatus();
        this.timestamp = reqeust.getTimestamp();
        this.value = reqeust.getValue();
        this.createDate = new Date();
        this.userId = reqeust.getUserId();
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
