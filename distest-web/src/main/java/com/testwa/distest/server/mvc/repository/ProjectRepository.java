/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mvc.repository;

import com.testwa.distest.server.mvc.model.Project;

import java.io.Serializable;
import java.util.List;

public interface ProjectRepository extends CommonRepository<Project, Serializable> {

    List<Project> findByUserId(String userId);

    Project findById(String projectId);

    Integer countByUserId(String id);
}
