package com.testwa.distest.server.service.task.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.Task;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
public interface ITaskDAO extends IBaseDAO<Task, Long> {
    List<Task> findBy(Task entity);
    Task findOne(Long entityId);

    List<Task> findByFromProject(Map<String, Object> params);

    Long countBy(Task kq);

    void updateEndTime(Long taskId, Date endTime);
}
