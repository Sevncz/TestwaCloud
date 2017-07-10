package com.testwa.distest.server.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by wen on 2016/9/24.
 */
@Document(collection = "t_report_sdetail")
public class ReportSdetail {
    @Id
    private String id;

    @Indexed
    private String detailId;

    @Indexed
    private String scriptId;

    @CreatedDate
    private Date startTime;

    private Date endTime;

    private Long totalTime;

    private String machineName;

    // portal 用户
    private String username;
    // 步骤运行状态，每有一个错误步骤+1
    private Integer stepStatus = 0;

    private Boolean disable = true;

    public ReportSdetail() {
    }

    public ReportSdetail(String detailId, String scriptId, String username) {
        this.detailId = detailId;
        this.scriptId = scriptId;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        this.totalTime = this.getTotalTime();

    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public Long getTotalTime() {
        if(this.totalTime == null){
            if(this.getEndTime() == null || this.getStartTime() == null){
                return null;
            }
            Calendar c1 = Calendar.getInstance();
            c1.clear();

            Calendar c2 = Calendar.getInstance();
            c2.clear();

            c1.setTime(this.getStartTime());
            c2.setTime(this.getEndTime());

            long totalTime = c2.getTimeInMillis() - c1.getTimeInMillis();

            this.setTotalTime(totalTime/1000);
        }
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    public Integer getStepStatus() {
        return stepStatus == null ? 0 : stepStatus;
    }

    public void setStepStatus(Integer stepStatus) {
        this.stepStatus = stepStatus;
    }

    public Boolean getDisable() {
        return disable;
    }

    public void setDisable(Boolean disable) {
        this.disable = disable;
    }
}
