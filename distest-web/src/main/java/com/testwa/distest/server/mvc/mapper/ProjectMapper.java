package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.mvc.entity.App;
import com.testwa.distest.server.mvc.entity.Project;

import java.util.List;

public interface ProjectMapper extends BaseMapper<Project, Long> {

	List<Project> findBy(Project project);

	List<Project> findAllByUser(Long userId);

    Integer countBy(Project query);
}