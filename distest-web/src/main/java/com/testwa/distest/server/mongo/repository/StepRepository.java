/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mongo.repository;

import com.testwa.distest.server.mongo.model.Step;

import java.io.Serializable;
import java.util.List;

public interface StepRepository extends CommonRepository<Step, Serializable> {

    List<Step> findByTaskId(Long taskId);

    List<Step> findByTaskIdAndDeviceId(Long taskId, String deviceId);
}
