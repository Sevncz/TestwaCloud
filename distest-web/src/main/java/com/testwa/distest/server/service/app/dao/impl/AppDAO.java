package com.testwa.distest.server.service.app.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.mapper.AppMapper;
import com.testwa.distest.server.service.app.dao.IAppDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class AppDAO extends BaseDAO<App, Long> implements IAppDAO {

    @Resource
    private AppMapper mapper;

    public List<App> findBy(App app) {
        return mapper.findBy(app);
    }

    @Override
    public App findOne(Long key) {
        return mapper.findOne(key);
    }

    @Override
    public List<App> findAll(List<Long> keys) {
        return mapper.findList(keys, null);
    }

    @Override
    public List<App> findByFromProject(Map<String, Object> params) {
        return mapper.findByFromProject(params);
    }

}