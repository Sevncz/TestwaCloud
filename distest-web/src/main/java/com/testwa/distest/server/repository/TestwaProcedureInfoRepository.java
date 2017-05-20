/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.TestwaProcedureInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;

public interface TestwaProcedureInfoRepository extends CommonRepository<TestwaProcedureInfo, Serializable> {

    List<TestwaProcedureInfo> findByReportDetailId(String infoId);

    List<TestwaProcedureInfo> findByReportDetailIdAndScriptId(String infoId, String scriptId);

    List<TestwaProcedureInfo> findByReportDetailIdOrderByTimestampAsc(String infoId);

    List<TestwaProcedureInfo> findByReportDetailIdAndScriptIdOrderByTimestampAsc(String infoId, String scriptId);

}
