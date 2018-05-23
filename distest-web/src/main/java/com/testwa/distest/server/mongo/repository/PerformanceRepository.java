/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mongo.repository;

import com.testwa.distest.server.mongo.model.Performance;
import com.testwa.distest.server.mongo.model.ProcedureInfo;

import java.io.Serializable;
import java.util.List;

public interface PerformanceRepository extends CommonRepository<Performance, Serializable> {

    List<Performance> findByTaskIdOrderByTimestampAsc(Long taskId);

    List<Performance> findByTaskIdAndDeviceIdOrderByTimestampAsc(Long taskId, String deviceId);
}
