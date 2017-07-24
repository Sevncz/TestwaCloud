package com.testwa.distest.server.mvc.vo;

import com.testwa.distest.server.mvc.model.ProcedureInfo;

/**
 * Created by wen on 2016/9/24.
 */
public class ReportStepInfoVO {

    private String action;
    private String id;
    private Integer status;

    public ReportStepInfoVO(ProcedureInfo info) {
        this.action = info.getAction();
        this.id = info.getId();
        this.status = info.getStatus();
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
