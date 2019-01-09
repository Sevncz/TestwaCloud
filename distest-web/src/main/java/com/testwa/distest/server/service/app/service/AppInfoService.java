package com.testwa.distest.server.service.app.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.condition.AppCondition;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.AppInfo;
import com.testwa.distest.server.mapper.AppInfoMapper;
import com.testwa.distest.server.mapper.AppMapper;
import com.testwa.distest.server.service.app.form.AppListForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by wen on 16/9/1.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
public class AppInfoService extends BaseService<AppInfo, Long> {
    private static final String REG_ANDROID_PACKAGE = "[a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*";
    @Autowired
    private AppMapper appMapper;
    @Autowired
    private AppInfoMapper appInfoMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void disableAppInfo(Long entityId){
        AppInfo appInfo = get(entityId);
        this.disableAppInfo(appInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void disableAppInfo(AppInfo appInfo) {
        if (appInfo != null) {
            disable(appInfo.getId());
            appMapper.disableAllBy(appInfo.getPackageName(), appInfo.getProjectId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAll(List<Long> entityIds){
        entityIds.forEach(this::disableAppInfo);
    }

    public AppInfo findOneInProject(Long entityId, Long projectId) {
        return appInfoMapper.findOneInProject(entityId, projectId);
    }

    public List<AppInfo> list(Long projectId, AppListForm queryForm) {
        AppInfo query = new AppInfo();
        query.setProjectId(projectId);
        if(StringUtils.isNotBlank(queryForm.getAppName())) {
            query.setName(queryForm.getAppName());
        }
        if(StringUtils.isNotBlank(queryForm.getPackageName())) {
            query.setPackageName(queryForm.getPackageName());
        }
        return appInfoMapper.findBy(query);
    }

    public PageResult<AppInfo> page(Long projectId, AppListForm queryForm){
        //分页处理
        AppInfo query = new AppInfo();
        query.setProjectId(projectId);
        if(StringUtils.isNotBlank(queryForm.getAppName())) {
            query.setName(queryForm.getAppName());
        }
        if(StringUtils.isNotBlank(queryForm.getPackageName())) {
            query.setPackageName(queryForm.getPackageName());
        }
        int offset = 0;
        if(queryForm.getPageNo() >= 1) {
            offset = (queryForm.getPageNo() - 1) * queryForm.getPageSize();
        }
        List<AppInfo> appList = appInfoMapper.findPage(query, queryForm.getOrderBy(), queryForm.getOrder(), offset, queryForm.getPageSize());
        Long total = appInfoMapper.countBy(query);
        PageResult<AppInfo> pr = new PageResult<>(appList, total);
        return pr;
    }

    public List<AppInfo> listByProjectId(Long projectId) {
        AppInfo query = new AppInfo();
        query.setProjectId(projectId);
        List<AppInfo> result = appInfoMapper.findBy(query);
        return result;
    }

    public List<AppInfo> findAll(List<Long> entityIds) {
        List<AppInfo> appInfos = new ArrayList<>();
        entityIds.forEach(id -> {
            AppInfo appInfo = get(id);
            appInfos.add(appInfo);
        });
        return appInfos;
    }

    public AppInfo getByPackage(Long projectId, String packageName) {
        AppCondition appCondition = new AppCondition();
        appCondition.setProjectId(projectId);
        appCondition.setPackageName(packageName);
        List<AppInfo> appInfos = appInfoMapper.selectByCondition(appCondition);
        if(appInfos.isEmpty()) {
            return null;
        }
        return appInfos.get(0);
    }

    public AppInfo getByDisplayName(Long projectId, String displayName) {
        AppCondition appCondition = new AppCondition();
        appCondition.setProjectId(projectId);
        appCondition.setDisplayName(displayName);
        List<AppInfo> appInfos = appInfoMapper.selectByCondition(appCondition);
        if(appInfos.isEmpty()) {
            return null;
        }
        return appInfos.get(0);
    }

    public AppInfo getByQuery(Long projectId, String query) {
        Pattern r = Pattern.compile(REG_ANDROID_PACKAGE);
        Matcher matcher = r.matcher(query);
        if (matcher.find()) {
            return getByPackage(projectId, query);
        }
        return getByDisplayName(projectId, query);
    }

    public void saveOrUpdateAppInfo(Long projectId, App app) {
        if(projectId == null) {
            return;
        }
        AppInfo appInfo = getByPackage(projectId, app.getPackageName());
        if(appInfo == null) {
            appInfo = new AppInfo();
            appInfo.setName(app.getDisplayName());
            appInfo.setPackageName(app.getPackageName());
            appInfo.setProjectId(app.getProjectId());
            appInfo.setLatestUploadTime(new Date());
            appInfo.setLatestAppId(app.getId());
            appInfo.setCreateTime(new Date());
            appInfo.setCreateBy(app.getCreateBy());
            appInfo.setPlatform(app.getPlatform());
            appInfo.setEnabled(true);
            appInfoMapper.insert(appInfo);
        }else{
            appInfo.setName(app.getDisplayName());
            appInfo.setLatestAppId(app.getId());
            appInfo.setLatestUploadTime(new Date());
            appInfo.setUpdateTime(new Date());
            appInfo.setUpdateBy(app.getCreateBy());
            appInfo.setPlatform(app.getPlatform());
            appInfo.setEnabled(true);
            appInfoMapper.update(appInfo);
        }
    }
}
