package com.testwa.distest.server.service.project.dao.impl;

import com.testwa.distest.common.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.ProjectMemberMapper;
import com.testwa.distest.server.service.project.dao.IProjectMemberDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProjectMemberDAO extends BaseDAO<ProjectMember, Long> implements IProjectMemberDAO {

    @Resource
    private ProjectMemberMapper mapper;

    @Override
    public void mergeInsert(List<ProjectMember> projectMembers) {
        mapper.mergeInsert(projectMembers);
    }

    @Override
    public List<ProjectMember> findByProjectIdAndMembers(Long projectId, List<Long> memberIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("members", memberIds);
        return mapper.findByProjectIdAndMembers(params);
    }

    @Override
    public int deleteMembersFromProject(Long projectId, List<Long> memberIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("members", memberIds);
        return mapper.deleteMembersFromProject(params);
    }

    @Override
    public int delete(Long projectId, Long memberId) {
        ProjectMember query = new ProjectMember();
        query.setProjectId(projectId);
        query.setMemberId(memberId);
        return mapper.deleteMemberFormProject(query);
    }

    @Override
    public List<User> findMembersFromProject(Long projectId) {
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        return mapper.findMembersFromProject(params);
    }

    @Override
    public List<User> findMembersFromProject(Long projectId, User userquery) {
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("user", userquery);
        return mapper.findMembersFromProject(params);
    }

    @Override
    public List<Map> findUsersProject(Long projectId, User userquery) {
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("user", userquery);
        return mapper.findUsersProject(params);
    }

    @Override
    public List<ProjectMember> findBy(ProjectMember query) {
        return mapper.findBy(query);
    }
}