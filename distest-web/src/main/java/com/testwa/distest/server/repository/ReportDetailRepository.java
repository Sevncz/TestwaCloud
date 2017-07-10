package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.ReportDetail;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
public interface ReportDetailRepository extends CommonRepository<ReportDetail, Serializable> {


    List<ReportDetail> findByReportId(String reportId);

    List<ReportDetail> findByReportIdOrderByIdDesc(String reportId);
}
