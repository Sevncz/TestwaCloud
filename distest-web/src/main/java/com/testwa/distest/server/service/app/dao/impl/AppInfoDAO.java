package com.testwa.distest.server.service.app.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.AppInfo;
import com.testwa.distest.server.mapper.AppInfoMapper;
import com.testwa.distest.server.mapper.AppMapper;
import com.testwa.distest.server.service.app.dao.IAppDAO;
import com.testwa.distest.server.service.app.dao.IAppInfoDAO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class AppInfoDAO extends BaseDAO<AppInfo, Long> implements IAppInfoDAO {

    @Resource
    private AppInfoMapper mapper;

    public List<AppInfo> findBy(AppInfo app) {
        return mapper.findBy(app);
    }

    @Override
    public AppInfo findOne(Long key) {
        return mapper.findOne(key);
    }

    @Override
    public List<AppInfo> findAll(List<Long> keys) {
        return mapper.findList(keys, null);
    }

    @Override
    public AppInfo findOneInProject(Long entityId, Long projectId) {
        return mapper.findOneInProject(entityId, projectId);
    }

    @Override
    public Long countBy(AppInfo query) {
        return mapper.countBy(query);
    }

    @Override
    public AppInfo findOne(Long projectId, String displayName, String packageName) {
        AppInfo query = new AppInfo();
        if(StringUtils.isNotBlank(packageName)) {
            query.setPackageName(packageName);
        }
        if(StringUtils.isNotBlank(displayName)) {
            query.setName(displayName);
        }
        query.setProjectId(projectId);
        List<AppInfo> appInfos = mapper.findBy(query);
        if(!appInfos.isEmpty()) {
            return appInfos.get(0);
        }
        return null;
    }

    @Override
    public void disableApp(Long entityId) {
        mapper.disable(entityId);
    }

    @Override
    public AppInfo getByPackage(Long projectId, String packageName) {
        return findOne(projectId, null, packageName);
    }

    @Override
    public AppInfo getByName(Long projectId, String name) {
        return findOne(projectId, name, null);
    }

    @Override
    public List<AppInfo> findPage(AppInfo query, String orderBy, String order, int offset, int pageSize) {
        return mapper.findPage(query, orderBy, order, offset, pageSize);
    }

}