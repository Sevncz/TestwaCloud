package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.entity.TaskSceneTestcase;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TaskSceneTestcaseMapper extends BaseMapper<TaskSceneTestcase, Long> {

	List<TaskSceneTestcase> findBy(TaskSceneTestcase entity);

    List<TaskSceneTestcase> findByFromProject(@Param("params") Map<String, Object> params);

    void insertAll(List<TaskSceneTestcase> entityList);

    int deleteByTaskSceneId(Long taskSceneId);
}