package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.core.entity.TaskTestcase;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TaskTestcaseMapper extends BaseMapper<TaskTestcase, Long> {

	List<TaskTestcase> findBy(TaskTestcase entity);

    List<TaskTestcase> findByFromProject(@Param("params") Map<String, Object> params);

    void insertAll(List<TaskTestcase> entityList);

}