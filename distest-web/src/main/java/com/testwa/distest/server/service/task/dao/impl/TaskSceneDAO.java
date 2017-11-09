package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.distest.server.entity.TaskScene;
import com.testwa.distest.common.dao.impl.BaseDAO;
import com.testwa.distest.server.mvc.mapper.TaskSceneMapper;
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
    public List<TaskScene> findByFromProject(Map<String, Object> params) {
        return mapper.findByFromProject(params);
    }

}