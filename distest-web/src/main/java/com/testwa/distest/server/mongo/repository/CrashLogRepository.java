/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mongo.repository;

import com.testwa.distest.server.mongo.model.CrashLog;
import com.testwa.distest.server.mongo.model.ExecutorLogInfo;
import com.testwa.distest.server.mongo.repository.CommonRepository;

import java.io.Serializable;
import java.util.List;

public interface CrashLogRepository extends CommonRepository<CrashLog, Serializable> {

    List<CrashLog> findByTaskId(Long taskId);
    List<CrashLog> findByTaskIdAndDeviceId(Long taskId, String deviceId);
}
