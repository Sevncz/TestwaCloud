package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.TaskSceneDetail;
import com.testwa.distest.server.mapper.TaskSceneTestcaseMapper;
import com.testwa.distest.server.service.task.dao.ITaskSceneDetailDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class TaskSceneDetailDAO extends BaseDAO<TaskSceneDetail, Long> implements ITaskSceneDetailDAO {

    @Resource
    private TaskSceneTestcaseMapper mapper;

    @Override
    public List<TaskSceneDetail> findBy(TaskSceneDetail entity) {
        return mapper.findBy(entity);
    }

    @Override
    public void insertAll(List<TaskSceneDetail> entityList) {
        mapper.insertAll(entityList);
    }

    @Override
    public int deleteByTaskSceneId(Long taskSceneId) {
        return mapper.deleteByTaskSceneId(taskSceneId);
    }
}