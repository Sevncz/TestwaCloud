package com.testwa.distest.server.mvc.model;

import io.rpc.testwa.task.ProcedureInfoRequest;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by wen on 16/8/27.
 */
@Data
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
    private Long taskId;
    @Indexed
    private Long testcaseId;
    @Indexed
    private Long scriptId;
    private String screenshotPath;
    private String description;
    private String params;
    private String action;
    @CreatedDate
    private Date createDate;
    @Indexed
    private Long timestamp;

    private Long userId;
    private String token;

    private String logcatFile;
    private Boolean disable = false;
    private Date modifyDate;

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
        this.token = reqeust.getToken();
        this.taskId = reqeust.getTaskId();
        this.testcaseId = reqeust.getTestcaseId();
    }

}
