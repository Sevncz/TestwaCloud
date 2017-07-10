package com.testwa.distest.server.web.VO;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.model.ReportSdetail;
import com.testwa.distest.server.model.Script;

import java.text.ParseException;

/**
 * Created by wen on 2016/9/24.
 */
public class ReportSdetailVO {

    private String id;
    private String detailId;
    private String scriptName;
    private String scriptId;
    private String startTime;
    private Long totalTime;
    // portal 用户
    private String username;
    private String status;

    public ReportSdetailVO(ReportSdetail sdetail, Script script) throws ParseException {
        this.id = sdetail.getId();
        this.detailId = sdetail.getDetailId();
        this.scriptId = sdetail.getScriptId();
        this.scriptName = script.getName();
        this.startTime = TimeUtil.formatTimeStamp(sdetail.getStartTime().getTime());
        this.totalTime = sdetail.getTotalTime();
        this.username = sdetail.getUsername();
        this.status = sdetail.getStepStatus() > 0 ?  "1" : "0";
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

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
