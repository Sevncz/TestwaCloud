/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mongo.repository;

import com.testwa.distest.server.mongo.model.TaskLog;

import java.io.Serializable;

public interface TaskLogRepository extends CommonRepository<TaskLog, Serializable> {

    TaskLog findByTaskCode(Long taskCode);

    void deleteByTaskCode(Long taskCode);
}
