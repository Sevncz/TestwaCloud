package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.TaskSceneTestcase;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TaskSceneTestcaseMapper extends BaseMapper<TaskSceneTestcase, Long> {

	List<TaskSceneTestcase> findBy(TaskSceneTestcase entity);

    List<TaskSceneTestcase> findByFromProject(@Param("params") Map<String, Object> params);

    void insertAll(List<TaskSceneTestcase> entityList);

    int deleteByTaskSceneId(Long taskSceneId);
}