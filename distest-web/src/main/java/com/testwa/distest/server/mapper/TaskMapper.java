package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.service.task.dto.CountAppTestStatisDTO;
import com.testwa.distest.server.service.task.dto.CountMemberTestStatisDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface TaskMapper extends BaseMapper<Task, Long> {

	List<Task> findBy(Task entity);

    Task findOne(Long key);

    Long countBy(Task query);

    void finish(@Param("taskCode") Long taskCode, @Param("endTime") Date endTime, @Param("status")DB.TaskStatus status);

    Task findByCode(Long taskCode);

    void disableAll(@Param("taskCodes") List<Long> taskCodes);

    List<Task> findFinishBy(@Param("query") Task query, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    List<CountAppTestStatisDTO> countAppTest(@Param("projectId") Long projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    List<CountMemberTestStatisDTO> countMemberTest(@Param("projectId") Long projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime);
}