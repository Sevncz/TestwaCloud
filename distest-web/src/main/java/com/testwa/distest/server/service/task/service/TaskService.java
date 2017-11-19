package com.testwa.distest.server.service.task.service;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mvc.model.ProcedureStatis;
import com.testwa.distest.server.service.task.dao.ITaskDAO;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by wen on 24/10/2017.
 */
@Log4j2
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class TaskService {

    @Autowired
    private ITaskDAO taskDAO;

    public long save(Task entity) {
        return taskDAO.insert(entity);
    }

    public Task findOne(Long entityId) {
        return taskDAO.findOne(entityId);
    }

    public List<Task> findAll(List<Long> entityIds) {
        return taskDAO.findAll(entityIds);
    }

    public void update(Task entity) {
        taskDAO.update(entity);
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

    public ProcedureStatis statis(Long taskId) {
        return null;
    }
}
