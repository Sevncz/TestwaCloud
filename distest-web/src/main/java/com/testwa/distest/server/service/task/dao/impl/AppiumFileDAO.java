package com.testwa.distest.server.service.task.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.AppiumFile;
import com.testwa.distest.server.mapper.AppiumFileMapper;
import com.testwa.distest.server.service.task.dao.IAppiumFileDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class AppiumFileDAO extends BaseDAO<AppiumFile, Long> implements IAppiumFileDAO {

    @Resource
    private AppiumFileMapper mapper;

    @Override
    public int removeFromTask(Long taskCode) {
        return mapper.removeFromTask(taskCode);
    }

    @Override
    public List<AppiumFile> findAll(Long taskCode) {
        AppiumFile query = new AppiumFile();
        query.setTaskCode(taskCode);
        return mapper.fildAll(query);
    }

    @Override
    public AppiumFile findOne(Long taskId, String deviceId) {
        return mapper.findOne(taskId, deviceId);
    }

}