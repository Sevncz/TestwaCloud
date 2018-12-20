package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.Project;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMapper extends BaseMapper<Project, Long> {

	List<Project> findBy(Project project);

	List<Project> findAllByUser(@Param("userId") Long userId, @Param("projectName") String projectName);

    long countBy(Project query);

    List<Project> findAllOrder(@Param("projectIds") List<Long> projectIds, @Param("order") String order);

    void disableAll(@Param("keys") List<Long> projectId);
}