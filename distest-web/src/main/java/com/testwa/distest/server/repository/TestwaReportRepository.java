package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.TestwaReport;
import com.testwa.distest.server.model.TestwaReportDetail;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;

/**
 * Created by wen on 16/9/1.
 */
public interface TestwaReportRepository extends CommonRepository<TestwaReport, Serializable> {


}
