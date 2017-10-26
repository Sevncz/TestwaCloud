package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.mvc.entity.ExecutionTask;
import com.testwa.distest.server.mvc.entity.Task;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ExecutionTaskMapper extends BaseMapper<ExecutionTask, Long> {

	List<ExecutionTask> findBy(ExecutionTask entity);

    List<ExecutionTask> findByFromProject(@Param("params") Map<String, Object> params);

}