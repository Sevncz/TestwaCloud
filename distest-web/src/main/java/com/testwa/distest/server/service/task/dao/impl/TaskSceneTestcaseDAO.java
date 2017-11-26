package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.TaskSceneTestcase;
import com.testwa.distest.server.mapper.TaskSceneTestcaseMapper;
import com.testwa.distest.server.service.task.dao.ITaskSceneTestcaseDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class TaskSceneTestcaseDAO extends BaseDAO<TaskSceneTestcase, Long> implements ITaskSceneTestcaseDAO {

    @Resource
    private TaskSceneTestcaseMapper mapper;

    @Override
    public List<TaskSceneTestcase> findBy(TaskSceneTestcase entity) {
        return mapper.findBy(entity);
    }

    @Override
    public void insertAll(List<TaskSceneTestcase> entityList) {
        mapper.insertAll(entityList);
    }

    @Override
    public int deleteByTaskSceneId(Long taskSceneId) {
        return mapper.deleteByTaskSceneId(taskSceneId);
    }
}