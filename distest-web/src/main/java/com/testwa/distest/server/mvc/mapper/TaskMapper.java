package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.mvc.entity.Script;
import com.testwa.distest.server.mvc.entity.Task;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TaskMapper extends BaseMapper<Task, Long> {

	List<Task> findBy(Task entity);

    List<Task> findByFromProject(@Param("params") Map<String, Object> params);

}