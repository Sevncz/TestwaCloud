package com.testwa.distest.server.service.app.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.AppInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 19/10/2017.
 */
public interface IAppInfoDAO extends IBaseDAO<AppInfo, Long> {
    List<AppInfo> findBy(AppInfo entity);

    AppInfo findOne(Long key);

    List<AppInfo> findAll(List<Long> keys);

    AppInfo findOneInProject(Long entityId, Long projectId);

    Long countBy(AppInfo query);

    AppInfo findOne(Long projectId, String displayName, String packageName);

    void disableApp(Long entityId);

    AppInfo getByPackage(Long projectId, String packageName);

    AppInfo getByName(Long projectId, String query);

    List<AppInfo> findPage(AppInfo query, String orderBy, String order, int offset, int pageSize);
}
