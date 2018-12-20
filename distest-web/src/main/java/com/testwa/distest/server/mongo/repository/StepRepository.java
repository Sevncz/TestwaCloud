/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mongo.repository;

import com.testwa.distest.server.mongo.model.Step;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository
public interface StepRepository extends CommonRepository<Step, Serializable> {

    List<Step> findByTaskCode(Long taskId);

    List<Step> findByTaskCodeAndDeviceId(Long taskId, String deviceId);
}
