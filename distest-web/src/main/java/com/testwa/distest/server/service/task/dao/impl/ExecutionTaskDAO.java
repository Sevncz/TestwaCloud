package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.distest.common.dao.impl.BaseDAO;
import com.testwa.distest.server.mvc.entity.ExecutionTask;
import com.testwa.distest.server.mvc.entity.Task;
import com.testwa.distest.server.mvc.mapper.ExecutionTaskMapper;
import com.testwa.distest.server.mvc.mapper.TaskMapper;
import com.testwa.distest.server.service.task.dao.IExecutionTaskDAO;
import com.testwa.distest.server.service.task.dao.ITaskDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class ExecutionTaskDAO extends BaseDAO<ExecutionTask, Long> implements IExecutionTaskDAO {

    @Resource
    private ExecutionTaskMapper mapper;

    public List<ExecutionTask> findBy(ExecutionTask app) {
        return mapper.findBy(app);
    }

    @Override
    public List<ExecutionTask> findByFromProject(Map<String, Object> params) {
        return mapper.findByFromProject(params);
    }

}