/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mongo.repository;

import com.testwa.distest.server.mongo.model.AppiumRunningLog;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository
public interface AppiumRunningLogRepository extends CommonRepository<AppiumRunningLog, Serializable> {

    List<AppiumRunningLog> findBySessionId(String sessionId);

    List<AppiumRunningLog> findByTaskCodeOrderByTimestampAsc(Long taskCode);

    List<AppiumRunningLog> findByTaskCodeAndDeviceIdOrderByTimestampAsc(Long taskCode, String deviceId);
}
