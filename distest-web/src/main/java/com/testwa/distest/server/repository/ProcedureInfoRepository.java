/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.ProcedureInfo;

import java.io.Serializable;
import java.util.List;

public interface ProcedureInfoRepository extends CommonRepository<ProcedureInfo, Serializable> {

    List<ProcedureInfo> findByReportDetailId(String infoId);

    List<ProcedureInfo> findByReportDetailIdAndScriptId(String infoId, String scriptId);

    List<ProcedureInfo> findByReportDetailIdOrderByTimestampAsc(String infoId);

    List<ProcedureInfo> findByReportDetailIdAndScriptIdOrderByTimestampAsc(String infoId, String scriptId);

}
