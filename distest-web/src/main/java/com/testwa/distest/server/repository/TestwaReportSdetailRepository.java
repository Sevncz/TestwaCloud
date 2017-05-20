package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.TestwaReportSdetail;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
public interface TestwaReportSdetailRepository extends CommonRepository<TestwaReportSdetail, Serializable> {


    TestwaReportSdetail findByDetailIdAndScriptId(String reportDetailId, String scriptId);

    List<TestwaReportSdetail> findByDetailId(String detailId);

    List<TestwaReportSdetail> findByDetailIdOrderById(String detailId);
}
