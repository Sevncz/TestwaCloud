package com.testwa.distest.server.service.task.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.TaskSceneDetail;

import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
public interface ITaskSceneDetailDAO extends IBaseDAO<TaskSceneDetail, Long> {
    List<TaskSceneDetail> findBy(TaskSceneDetail entity);

    void insertAll(List<TaskSceneDetail> entityList);

    int deleteByTaskSceneId(Long taskSceneId);
}
