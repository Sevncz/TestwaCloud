package com.testwa.distest.server.service.task.dao;

import com.testwa.distest.server.entity.TaskScene;
import com.testwa.core.base.dao.IBaseDAO;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
public interface ITaskSceneDAO extends IBaseDAO<TaskScene, Long> {
    List<TaskScene> findBy(TaskScene entity);

    TaskScene findOne(Long key);

    List<TaskScene> findAll(List<Long> keys);

    List<TaskScene> findByFromProject(Map<String, Object> params);

    /**
     * fetch app, createUser and updateUser
     * @param taskSceneId
     * @return
     */
    TaskScene fetchOne(Long taskSceneId);

    /**
     * fetch detail
     * @param taskSceneId
     * @return
     */
    TaskScene fetchOneForDetail(Long taskSceneId);
}
