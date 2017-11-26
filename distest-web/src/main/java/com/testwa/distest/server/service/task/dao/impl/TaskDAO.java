package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.distest.server.entity.Task;
import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.mapper.TaskMapper;
import com.testwa.distest.server.service.task.dao.ITaskDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class TaskDAO extends BaseDAO<Task, Long> implements ITaskDAO {

    @Resource
    private TaskMapper mapper;

    public List<Task> findBy(Task entity) {
        return mapper.findBy(entity);
    }

    @Override
    public List<Task> findByFromProject(Map<String, Object> params) {
        return mapper.findByFromProject(params);
    }

}