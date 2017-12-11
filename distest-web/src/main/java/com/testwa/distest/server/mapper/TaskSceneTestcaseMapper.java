package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.TaskSceneDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TaskSceneTestcaseMapper extends BaseMapper<TaskSceneDetail, Long> {

	List<TaskSceneDetail> findBy(TaskSceneDetail entity);

    List<TaskSceneDetail> findByFromProject(@Param("params") Map<String, Object> params);

    void insertAll(List<TaskSceneDetail> entityList);

    int deleteByTaskSceneId(Long taskSceneId);
}