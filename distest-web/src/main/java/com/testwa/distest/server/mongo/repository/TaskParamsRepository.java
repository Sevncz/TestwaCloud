/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mongo.repository;

import com.testwa.distest.server.mongo.model.TaskParams;

import java.io.Serializable;

public interface TaskParamsRepository extends CommonRepository<TaskParams, Serializable> {

    TaskParams findByTaskCode(Long taskCode);

    void deleteByTaskCode(Long taskCode);
}
