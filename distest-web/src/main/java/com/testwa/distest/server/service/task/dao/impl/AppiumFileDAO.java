package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.AppiumFile;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.mapper.AppiumFileMapper;
import com.testwa.distest.server.mapper.TaskMapper;
import com.testwa.distest.server.service.task.dao.IAppiumFileDAO;
import com.testwa.distest.server.service.task.dao.ITaskDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class AppiumFileDAO extends BaseDAO<AppiumFile, Long> implements IAppiumFileDAO {

    @Resource
    private AppiumFileMapper mapper;

    @Override
    public int removeFromTask(Long taskId) {
        return mapper.removeFromTask(taskId);
    }

    @Override
    public List<AppiumFile> findAll(Long taskId) {
        AppiumFile query = new AppiumFile();
        query.setTaskId(taskId);
        return mapper.fildAll(query);
    }

    @Override
    public AppiumFile findOne(Long taskId, String deviceId) {
        return mapper.findOne(taskId, deviceId);
    }

}