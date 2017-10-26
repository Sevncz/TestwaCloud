package com.testwa.distest.server.service.task.dao;

import com.testwa.distest.common.dao.IBaseDAO;
import com.testwa.distest.server.mvc.entity.ExecutionTask;
import com.testwa.distest.server.mvc.entity.Task;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
public interface IExecutionTaskDAO extends IBaseDAO<ExecutionTask, Long> {
    List<ExecutionTask> findBy(ExecutionTask entity);

    List<ExecutionTask> findByFromProject(Map<String, Object> params);
}
