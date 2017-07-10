package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.ReportSdetail;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
public interface ReportSdetailRepository extends CommonRepository<ReportSdetail, Serializable> {


    ReportSdetail findByDetailIdAndScriptId(String reportDetailId, String scriptId);

    List<ReportSdetail> findByDetailId(String detailId);

    List<ReportSdetail> findByDetailIdOrderById(String detailId);
}
