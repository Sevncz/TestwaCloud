/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mvc.repository;

import com.testwa.distest.server.mvc.model.ProcedureInfo;

import java.io.Serializable;
import java.util.List;

public interface ProcedureInfoRepository extends CommonRepository<ProcedureInfo, Serializable> {

    List<ProcedureInfo> findByExecutionTaskIdOrderByTimestampAsc(String infoId);

    List<ProcedureInfo> findByExecutionTaskIdAndScriptIdOrderByTimestampAsc(String infoId, String scriptId);

    List<ProcedureInfo> findBySessionId(String sessionId);

    List<ProcedureInfo> findByDeviceIdAndExecutionTaskIdAndTestcaseIdAndScriptId(String deviceId, String exeId, String caseId, String scriptId);
}
