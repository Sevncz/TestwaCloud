package com.testwa.distest.server.service.app.dao.impl;

import com.testwa.distest.common.dao.impl.BaseDAO;
import com.testwa.distest.server.mvc.entity.App;
import com.testwa.distest.server.mvc.mapper.AppMapper;
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
    public List<App> findByFromProject(Map<String, Object> params) {
        return mapper.findByFromProject(params);
    }

}