package com.testwa.distest.server.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ProjectMemberMapper extends BaseMapper<ProjectMember, Long> {

    int mergeInsert(List<ProjectMember> projectMembers);

    List<ProjectMember> findByProjectIdAndMembers(@Param("params") Map<String, Object> params);

    int deleteMembersFromProject(Map<String, Object> params);

    int deleteMemberFormProject(ProjectMember query);

    List<User> findMembersFromProject(@Param("params") Map<String, Object> params);

    List<Map> findUsersProject(@Param("params") Map<String, Object> params);

    List<ProjectMember> findBy(ProjectMember query);
}