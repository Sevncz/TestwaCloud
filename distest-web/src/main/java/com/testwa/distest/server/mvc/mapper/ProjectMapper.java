package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.core.entity.App;
import com.testwa.core.entity.Project;

import java.util.List;

public interface ProjectMapper extends BaseMapper<Project, Long> {

	List<Project> findBy(Project project);

	List<Project> findAllByUser(Long userId);

    Integer countBy(Project query);
}