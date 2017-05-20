/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.repository;

import com.testwa.distest.server.model.TestwaAgent;
import com.testwa.distest.server.model.TestwaProject;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;

public interface TestwaAgentRepository extends CommonRepository<TestwaAgent, Serializable> {

    List<TestwaAgent> findById(String Id);

    TestwaAgent findByMac(String mac);
}
