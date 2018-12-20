package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.ProjectMember;
import com.testwa.distest.server.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface ProjectMemberMapper extends BaseMapper<ProjectMember, Long> {

    int mergeInsert(List<ProjectMember> projectMembers);

    List<ProjectMember> findByProjectIdAndMembers(@Param("projectId") Long projectId, @Param("memberIds") List<Long> memberIds);
    ProjectMember findByProjectIdAndMember(@Param("projectId") Long projectId, @Param("memberId") Long memberId);

    int deleteMemberList(@Param("projectId") Long projectId, @Param("memberIds") Set<Long> memberIds);

    int deleteMember(@Param("projectId") Long projectId, @Param("memberId") Long memberId);

    List<User> findMembersFromProject(@Param("params") Map<String, Object> params);

    List<Map> findUsersProject(@Param("projectId") Long projectId, @Param("user") User user);

    List<ProjectMember> findBy(ProjectMember query);
}