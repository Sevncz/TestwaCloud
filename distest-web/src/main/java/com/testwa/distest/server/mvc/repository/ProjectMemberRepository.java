/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testwa.distest.server.mvc.repository;

import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.ProjectMember;

import java.io.Serializable;
import java.util.List;

public interface ProjectMemberRepository extends CommonRepository<ProjectMember, Serializable> {

    List<ProjectMember> findByProjectId(String projectId);

    ProjectMember findByProjectIdAndMemberId(String projectId, String memberId);

    List<ProjectMember> findByMemberId(String id);
}
