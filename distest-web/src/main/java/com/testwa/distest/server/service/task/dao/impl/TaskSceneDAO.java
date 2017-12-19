package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.distest.server.entity.TaskScene;
import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.mapper.TaskSceneMapper;
import com.testwa.distest.server.service.task.dao.ITaskSceneDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class TaskSceneDAO extends BaseDAO<TaskScene, Long> implements ITaskSceneDAO {

    @Resource
    private TaskSceneMapper mapper;

    public List<TaskScene> findBy(TaskScene app) {
        return mapper.findBy(app);
    }

    @Override
    public TaskScene findOne(Long key) {
        return mapper.findOne(key);
    }

    @Override
    public List<TaskScene> findAll(List<Long> keys) {
        return mapper.findList(keys, null);
    }

    @Override
    public List<TaskScene> findByFromProject(Map<String, Object> params) {
        return mapper.findByFromProject(params);
    }

    @Override
    public TaskScene fetchOne(Long taskSceneId) {
        return mapper.fetchOne(taskSceneId);
    }

    @Override
    public TaskScene fetchOneForDetail(Long taskSceneId) {
        return mapper.fetchOneForDetail(taskSceneId);
    }

}