/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mongo.repository;

import com.testwa.distest.server.mongo.model.AppiumRunningLog;

import java.io.Serializable;
import java.util.List;

public interface AppiumRunningLogRepository extends CommonRepository<AppiumRunningLog, Serializable> {

    List<AppiumRunningLog> findBySessionId(String sessionId);

    List<AppiumRunningLog> findByExecutionTaskIdOrderByTimestampAsc(Long executionTaskId);

    List<AppiumRunningLog> findByExecutionTaskIdAndDeviceIdOrderByTimestampAsc(Long id, String k);
}
