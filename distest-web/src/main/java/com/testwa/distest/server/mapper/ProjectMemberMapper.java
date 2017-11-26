package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ProjectMemberMapper extends BaseMapper<ProjectMember, Long> {

    int mergeInsert(List<ProjectMember> projectMembers);

    List<ProjectMember> findByProjectIdAndMembers(@Param("params") Map<String, Object> params);

    int deleteMembersFromProject(Map<String, Object> params);

    int deleteMemberFormProject(ProjectMember query);

    List<User> findMembersFromProject(@Param("params") Map<String, Object> params);

    List<Map> findUsersProject(@Param("params") Map<String, Object> params);

    List<ProjectMember> findBy(ProjectMember query);
}