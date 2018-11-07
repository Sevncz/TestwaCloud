package com.testwa.distest.server.service.project.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wen on 19/10/2017.
 */
public interface IProjectMemberDAO extends IBaseDAO<ProjectMember, Long> {

    void mergeInsert(List<ProjectMember> projectMembers);

    List<ProjectMember> findByProjectIdAndMembers(Long projectId, List<Long> memberIds);

    ProjectMember findByProjectIdAndMember(Long projectId, Long memberId);

    int deleteMemberList(Long projectId, Set<Long> memberIds);

    int deleteMember(Long projectId, Long memberId);

    List<User> findMembersFromProject(Long projectId);

    List<User> findMembersFromProject(Long projectId, User userquery);

    List<Map> findUsersProject(Long projectId, User userquery);

    List<ProjectMember> findBy(ProjectMember query);
}
