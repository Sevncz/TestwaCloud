package com.testwa.distest.server.web.app.validator;

import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.AppInfo;
import com.testwa.distest.server.service.app.service.AppInfoService;
import com.testwa.distest.server.service.app.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wen on 23/10/2017.
 */
@Component
public class AppValidator {

    @Autowired
    private AppService appService;
    @Autowired
    private AppInfoService appInfoService;

    public void validateAppsExist(List<Long> entityIds) throws ObjectNotExistsException {
        List<App> entityList = appService.findAll(entityIds);
        if(entityList == null || entityList.size() != entityIds.size()){
            throw new ObjectNotExistsException("App不存在");
        }
    }

    public void validateAppExist(Long entityId) throws ObjectNotExistsException {
        App entity = appService.findOne(entityId);
        if(entity == null){
            throw new ObjectNotExistsException("App不存在");
        }
    }

    public void validateAppInPorject(Long entityId, Long projectId) throws ObjectNotExistsException {
        App entity = appService.findOneInProject(entityId, projectId);
        if(entity == null){
            throw new ObjectNotExistsException("App不在该项目中");
        }
    }

    public AppInfo validateAppInfoExist(Long entityId) {
        AppInfo entity = appInfoService.findOne(entityId);
        if(entity == null){
            throw new ObjectNotExistsException("App不存在");
        }
        return entity;
    }
}
