package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.Task;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface TaskMapper extends BaseMapper<Task, Long> {

	List<Task> findBy(Task entity);

    List<Task> findByFromProject(@Param("params") Map<String, Object> params);

    Task findOne(Long key);

    Long countBy(Task query);

    void updateEndTime(@Param("taskId") Long taskId, @Param("endTime") Date endTime);
}