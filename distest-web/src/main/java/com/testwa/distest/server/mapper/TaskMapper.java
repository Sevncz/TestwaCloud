package com.testwa.distest.server.mapper;

import com.testwa.distest.server.entity.Task;
import com.testwa.distest.common.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TaskMapper extends BaseMapper<Task, Long> {

	List<Task> findBy(Task entity);

    List<Task> findByFromProject(@Param("params") Map<String, Object> params);

}