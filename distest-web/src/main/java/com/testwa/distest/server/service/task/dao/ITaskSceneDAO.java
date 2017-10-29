package com.testwa.distest.server.service.task.dao;

import com.testwa.core.entity.TaskScene;
import com.testwa.distest.common.dao.IBaseDAO;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
public interface ITaskSceneDAO extends IBaseDAO<TaskScene, Long> {
    List<TaskScene> findBy(TaskScene entity);

    List<TaskScene> findByFromProject(Map<String, Object> params);
}
