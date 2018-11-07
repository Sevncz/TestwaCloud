package com.testwa.distest.server.service.project.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.ProjectMemberMapper;
import com.testwa.distest.server.service.project.dao.IProjectMemberDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        return mapper.findByProjectIdAndMembers(projectId, memberIds);
    }

    @Override
    public ProjectMember findByProjectIdAndMember(Long projectId, Long memberId) {
        return mapper.findByProjectIdAndMember(projectId, memberId);
    }

    @Override
    public int deleteMemberList(Long projectId, Set<Long> memberIds) {
        return mapper.deleteMemberList(projectId, memberIds);
    }

    @Override
    public int deleteMember(Long projectId, Long memberId) {
        return mapper.deleteMember(projectId, memberId);
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
        return mapper.findUsersProject(projectId, userquery);
    }

    @Override
    public List<ProjectMember> findBy(ProjectMember query) {
        return mapper.findBy(query);
    }
}