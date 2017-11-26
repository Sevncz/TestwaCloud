package com.testwa.distest.server.service.task.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.Task;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
public interface ITaskDAO extends IBaseDAO<Task, Long> {
    List<Task> findBy(Task entity);

    List<Task> findByFromProject(Map<String, Object> params);
}
