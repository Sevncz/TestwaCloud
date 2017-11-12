package com.testwa.distest.server.service.task.dao;

import com.testwa.distest.common.dao.IBaseDAO;
import com.testwa.distest.server.entity.TaskSceneTestcase;

import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
public interface ITaskSceneTestcaseDAO extends IBaseDAO<TaskSceneTestcase, Long> {
    List<TaskSceneTestcase> findBy(TaskSceneTestcase entity);

    void insertAll(List<TaskSceneTestcase> entityList);

    int deleteByTaskSceneId(Long taskSceneId);
}
