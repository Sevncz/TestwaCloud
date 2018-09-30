package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Task;
import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.mapper.TaskMapper;
import com.testwa.distest.server.service.task.dao.ITaskDAO;
import com.testwa.distest.server.service.task.dto.CountAppTestStatisDTO;
import com.testwa.distest.server.service.task.dto.CountElapsedTimeStatisDTO;
import com.testwa.distest.server.service.task.dto.CountMemberTestStatisDTO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class TaskDAO extends BaseDAO<Task, Long> implements ITaskDAO {

    @Resource
    private TaskMapper mapper;

    public List<Task> findBy(Task entity) {
        return mapper.findBy(entity);
    }

    @Override
    public Task findOne(Long entityId) {
        return mapper.findOne(entityId);
    }

    @Override
    public Long countBy(Task query) {
        return mapper.countBy(query);
    }

    @Override
    public void finish(Long taskCode, Date endTime, DB.TaskStatus status) {
        mapper.finish(taskCode, endTime, status);
    }

    @Override
    public List<Task> findFinishBy(Task query) {
        return mapper.findFinishBy(query, null, null);
    }

    @Override
    public List<Task> findFinishBy(Task query, Long startTime, Long endTime) {
        return mapper.findFinishBy(query, startTime, endTime);
    }

    @Override
    public List<CountAppTestStatisDTO> countAppTest(Long projectId, Long startTime, Long endTime) {
        return mapper.countAppTest(projectId, startTime, endTime);
    }

    @Override
    public List<CountMemberTestStatisDTO> countMemberTest(Long projectId, Long startTime, Long endTime) {
        return mapper.countMemberTest(projectId, startTime, endTime);
    }

    @Override
    public List<CountElapsedTimeStatisDTO> countElapsedTimeByDay(Long projectId, Long startTime, Long endTime) {
        return mapper.countElapsedTimeByDay(projectId, null, startTime, endTime);
    }

    @Override
    public List<CountElapsedTimeStatisDTO> countElapsedTimeByDay(Long projectId, Long userId, Long startTime, Long endTime) {
        return mapper.countElapsedTimeByDay(projectId, userId, startTime, endTime);
    }

    @Override
    public Task findByCode(Long taskCode) {
        return mapper.findByCode(taskCode);
    }

    @Override
    public void disableAll(List<Long> taskCodes) {
        mapper.disableAll(taskCodes);
    }

}