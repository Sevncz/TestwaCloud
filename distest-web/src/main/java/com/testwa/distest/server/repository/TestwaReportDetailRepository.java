package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.TestwaReportDetail;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wen on 16/9/1.
 */
public interface TestwaReportDetailRepository extends CommonRepository<TestwaReportDetail, Serializable> {


    List<TestwaReportDetail> findByReportId(String reportId);

    List<TestwaReportDetail> findByReportIdOrderByIdDesc(String reportId);
}
