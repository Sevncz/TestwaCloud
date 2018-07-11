package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.LogFile;
import com.testwa.distest.server.mapper.LogFileMapper;
import com.testwa.distest.server.service.task.dao.ILogFileDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class LogFileDAO extends BaseDAO<LogFile, Long> implements ILogFileDAO {

    @Resource
    private LogFileMapper mapper;

    @Override
    public int removeFromTask(Long taskId) {
        return mapper.removeFromTask(taskId);
    }

    @Override
    public List<LogFile> findAll(Long taskId) {
        LogFile query = new LogFile();
        query.setTaskCode(taskId);
        return mapper.fildAll(query);
    }

    @Override
    public LogFile findOne(Long taskId, String deviceId) {
        return mapper.findOne(taskId, deviceId);
    }

}