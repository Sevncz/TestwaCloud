package com.testwa.distest.server.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.entity.Project;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProjectMapper extends BaseMapper<Project, Long> {

	List<Project> findBy(Project project);

	List<Project> findAllByUser(@Param("userId") Long userId, @Param("projectName") String projectName);

    long countBy(Project query);
}