package com.testwa.distest.server.service.task.service;

import com.testwa.core.common.enums.DB;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.common.exception.NoSuchExecutionTaskException;
import com.testwa.core.entity.*;
import com.testwa.distest.server.mvc.model.ProcedureStatis;
import com.testwa.distest.server.service.task.dao.ITaskDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by wen on 24/10/2017.
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class TaskService {

    @Autowired
    private ITaskDAO taskDAO;


    public long save(Task exeTask) {
        return taskDAO.insert(exeTask);
    }

    public Task findOne(Long exeId) {
        return taskDAO.findOne(exeId);
    }

    public List<Task> findAll(List<Long> entityIds) {
        return taskDAO.findAll(entityIds);
    }

    public void update(Task task) {
        taskDAO.update(task);
    }

    public List<Task> getRunningTask(Long projectId, Long userId) {
        Task query = new Task();
        query.setStatus(DB.TaskStatus.RUNNING);
        query.setProjectId(projectId);
        query.setCreateBy(userId);
        return taskDAO.findBy(query);
    }

    public List<Task> getRecentFinishedRunningTask(Long projectId, Long userId) {
        Task query = new Task();
        query.setStatus(DB.TaskStatus.COMPLETE);
        query.setProjectId(projectId);
        query.setCreateBy(userId);
        return taskDAO.findBy(query);
    }

}
