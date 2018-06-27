package com.testwa.distest.server.service.app.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.App;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
public interface IAppDAO extends IBaseDAO<App, Long> {
    List<App> findBy(App entity);

    App findOne(Long key);

    List<App> findAll(List<Long> keys);

    List<App> findByFromProject(Map<String, Object> params);

    App findOneInProject(Long entityId, Long projectId);

    Long countBy(App query);

    void disableAll(String packageName, Long projectId);

    List<App> getAllVersion(String packageName, Long projectId);
}
