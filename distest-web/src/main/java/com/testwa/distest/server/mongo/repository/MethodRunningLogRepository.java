/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mongo.repository;

import com.testwa.distest.server.mongo.model.MethodRunningLog;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository
public interface MethodRunningLogRepository extends CommonRepository<MethodRunningLog, Serializable> {

    List<MethodRunningLog> findByTaskCodeOrderByTimestampAsc(Long taskId);

    List<MethodRunningLog> findByTaskCodeAndDeviceIdOrderByMethodOrderAsc(Long taskId, String deviceId);
}
