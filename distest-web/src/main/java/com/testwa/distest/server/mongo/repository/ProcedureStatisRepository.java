/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mongo.repository;

import com.testwa.distest.server.mongo.model.ProcedureStatis;

import java.io.Serializable;

public interface ProcedureStatisRepository extends CommonRepository<ProcedureStatis, Serializable> {

    ProcedureStatis findByTaskCode(Long taskCode);

}
