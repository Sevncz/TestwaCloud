/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mongo.repository;

import com.testwa.distest.server.mongo.model.Performance;

import java.io.Serializable;
import java.util.List;

public interface PerformanceRepository extends CommonRepository<Performance, Serializable> {

    List<Performance> findByTaskCodeOrderByTimestampAsc(Long taskId);

    List<Performance> findByTaskCodeAndDeviceIdOrderByTimestampAsc(Long taskId, String deviceId);
}
