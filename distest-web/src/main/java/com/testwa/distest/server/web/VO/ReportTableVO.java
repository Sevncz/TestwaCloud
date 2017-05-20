package com.testwa.distest.server.web.VO;

import com.testwa.distest.server.model.TestwaReport;
import com.testwa.distest.server.model.TestwaReportDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wen on 2016/9/24.
 */
public class ReportTableVO {

    private String id;
    private String appName;
    private String appVersion;
    private String name;
    private String status;
    private Long success;
    private Long fail;

    private List<ReportDetailVO> details;

    public ReportTableVO(TestwaReport report, List<TestwaReportDetail> details) {
        this.id = report.getId();
        this.appName = report.getA_name();
        this.appVersion = report.getA_version();
        this.name = report.getTestcaseName();
        this.details = new ArrayList<>();
        for(TestwaReportDetail d : details){
            ReportDetailVO dvo = new ReportDetailVO(d);
            this.details.add(dvo);
        }
        this.status = report.getErrorInfo().size() == 0 ? "成功" : "失败";

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ReportDetailVO> getDetails() {
        return details;
    }

    public void setDetails(List<ReportDetailVO> details) {
        this.details = details;
    }

    public Long getSuccess() {
        return success;
    }

    public void setSuccess(Long success) {
        this.success = success;
    }

    public Long getFail() {
        return fail;
    }

    public void setFail(Long fail) {
        this.fail = fail;
    }
}
