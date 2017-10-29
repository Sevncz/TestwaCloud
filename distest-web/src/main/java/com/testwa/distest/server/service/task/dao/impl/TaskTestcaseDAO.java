package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.distest.common.dao.impl.BaseDAO;
import com.testwa.core.entity.TaskTestcase;
import com.testwa.distest.server.mvc.mapper.TaskTestcaseMapper;
import com.testwa.distest.server.service.task.dao.ITaskTestcaseDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class TaskTestcaseDAO extends BaseDAO<TaskTestcase, Long> implements ITaskTestcaseDAO {

    @Resource
    private TaskTestcaseMapper mapper;

    @Override
    public List<TaskTestcase> findBy(TaskTestcase entity) {
        return mapper.findBy(entity);
    }

    @Override
    public void insertAll(List<TaskTestcase> entityList) {
        mapper.insertAll(entityList);
    }
}