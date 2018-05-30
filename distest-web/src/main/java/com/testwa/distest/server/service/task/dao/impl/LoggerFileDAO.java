package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.LoggerFile;
import com.testwa.distest.server.mapper.LoggerFileMapper;
import com.testwa.distest.server.service.task.dao.ILoggerFileDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class LoggerFileDAO extends BaseDAO<LoggerFile, Long> implements ILoggerFileDAO {

    @Resource
    private LoggerFileMapper mapper;

    @Override
    public int removeFromTask(Long taskId) {
        return mapper.removeFromTask(taskId);
    }

    @Override
    public List<LoggerFile> findAll(Long taskId) {
        LoggerFile query = new LoggerFile();
        query.setTaskCode(taskId);
        return mapper.fildAll(query);
    }

    @Override
    public LoggerFile findOne(Long taskId, String deviceId) {
        return mapper.findOne(taskId, deviceId);
    }

}