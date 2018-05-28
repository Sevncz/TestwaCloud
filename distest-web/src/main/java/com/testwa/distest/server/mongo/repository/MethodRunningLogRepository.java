/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mongo.repository;

import com.testwa.distest.server.mongo.model.MethodRunningLog;

import java.io.Serializable;
import java.util.List;

public interface MethodRunningLogRepository extends CommonRepository<MethodRunningLog, Serializable> {

    List<MethodRunningLog> findByTaskIdOrderByTimestampAsc(Long taskId);

    List<MethodRunningLog> findByTaskIdAndDeviceIdOrderByMethodOrderAsc(Long taskId, String deviceId);
}
