package com.testwa.distest.server.web.VO;

import com.testwa.distest.server.model.TestwaProcedureInfo;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

/**
 * Created by wen on 2016/9/24.
 */
public class StepInfoVO {

    private String id;
    private Integer status;
    private Integer runtime;
    private String screenshotPath;
    private String lastScreenshotPath;
    private String params;
    private String action;


    public StepInfoVO(TestwaProcedureInfo info, TestwaProcedureInfo last) {
        this.id = info.getId();
        this.status = info.getStatus();
        this.runtime = info.getRuntime();
        this.screenshotPath = info.getScreenshotPath();
        this.params = info.getParams();
        this.action = info.getAction();
        if(last != null){
            this.lastScreenshotPath = last.getScreenshotPath();
        }else{
            this.lastScreenshotPath = "";
        }
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

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getLastScreenshotPath() {
        return lastScreenshotPath;
    }

    public void setLastScreenshotPath(String lastScreenshotPath) {
        this.lastScreenshotPath = lastScreenshotPath;
    }
}
