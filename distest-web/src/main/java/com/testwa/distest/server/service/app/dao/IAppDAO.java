package com.testwa.distest.server.service.app.dao;

import com.testwa.distest.common.dao.IBaseDAO;
import com.testwa.core.entity.App;
import com.testwa.core.entity.Role;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
public interface IAppDAO extends IBaseDAO<App, Long> {
    List<App> findBy(App entity);

    List<App> findByFromProject(Map<String, Object> params);
}
