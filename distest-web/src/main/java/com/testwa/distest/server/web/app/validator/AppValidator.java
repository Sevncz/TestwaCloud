package com.testwa.distest.server.web.app.validator;

import com.testwa.distest.common.exception.NoSuchProjectException;
import com.testwa.core.entity.App;
import com.testwa.core.entity.Project;
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

    public void validateProject(List<Long> entityIds) throws NoSuchProjectException {
        List<App> entityList = appService.findAll(entityIds);
        if(entityList == null || entityList.size() != entityIds.size()){
            throw new NoSuchProjectException("项目不存在");
        }
    }

    public void validateProject(Long projectId) throws NoSuchProjectException {
        App entity = appService.findOne(projectId);
        if(entity == null){
            throw new NoSuchProjectException("项目不存在");
        }
    }

}
