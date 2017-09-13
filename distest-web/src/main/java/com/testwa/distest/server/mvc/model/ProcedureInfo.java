package com.testwa.distest.server.mvc.model;

import io.rpc.testwa.task.ProcedureInfoRequest;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by wen on 16/8/27.
 */
@Document(collection = "t_procedure_info")
public class ProcedureInfo {
    @Id
    private String id;
    private Integer status;
    private String value;
    private Integer runtime;
    private Integer cpurate;
    private Integer memory;
    @Indexed
    private String sessionId;
    @Indexed
    private String deviceId;
    @Indexed
    private String executionTaskId;
    @Indexed
    private String testcaseId;
    @Indexed
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
    private Boolean disable = false;
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

    public Integer getCpurate() {
        return cpurate;
    }

    public void setCpurate(Integer cpurate) {
        this.cpurate = cpurate;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
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

    public void toEntity(ProcedureInfoRequest reqeust) {
        this.action = reqeust.getActionBytes().toStringUtf8();
//        this.cpurate = reqeust.getCpurate();
        this.description = reqeust.getDescription();
        this.deviceId = reqeust.getDeviceId();
        this.logcatFile = reqeust.getLogcatFile();
//        this.memory = reqeust.getMemory();
        this.params = reqeust.getParams();
        this.runtime = reqeust.getRuntime();
        this.screenshotPath = reqeust.getScreenshotPath();
        this.sessionId = reqeust.getSessionId();
        this.scriptId = reqeust.getScriptId();
        this.status = reqeust.getStatus();
        this.timestamp = reqeust.getTimestamp();
        this.value = reqeust.getValue();
        this.createDate = new Date();
        this.userId = reqeust.getUserId();
        this.executionTaskId = reqeust.getExecutionTaskId();
        this.testcaseId = reqeust.getTestcaseId();
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

    public String getExecutionTaskId() {
        return executionTaskId;
    }

    public void setExecutionTaskId(String executionTaskId) {
        this.executionTaskId = executionTaskId;
    }

    public String getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(String testcaseId) {
        this.testcaseId = testcaseId;
    }
}
