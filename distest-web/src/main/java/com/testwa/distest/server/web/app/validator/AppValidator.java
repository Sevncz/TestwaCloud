package com.testwa.distest.server.web.app.validator;

import com.testwa.distest.common.exception.NoSuchProjectException;
import com.testwa.distest.server.entity.App;
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

    public void validateAppsExist(List<Long> entityIds) throws NoSuchProjectException {
        List<App> entityList = appService.findAll(entityIds);
        if(entityList == null || entityList.size() != entityIds.size()){
            throw new NoSuchProjectException("App不存在");
        }
    }

    public void validateAppExist(Long entityId) throws NoSuchProjectException {
        App entity = appService.findOne(entityId);
        if(entity == null){
            throw new NoSuchProjectException("App不存在");
        }
    }

}
