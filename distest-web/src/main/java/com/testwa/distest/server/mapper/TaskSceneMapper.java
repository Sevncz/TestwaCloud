package com.testwa.distest.server.mapper;

import com.testwa.distest.server.entity.TaskScene;
import com.testwa.core.base.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TaskSceneMapper extends BaseMapper<TaskScene, Long> {

	List<TaskScene> findBy(TaskScene entity);

    List<TaskScene> findByFromProject(@Param("params") Map<String, Object> params);

    TaskScene findOne(Long key);

    List<TaskScene> findList(@Param("keys") List<Long> keys, @Param("orderBy") String orderBy);

    TaskScene fetchOne(Long taskSceneId);
}