package com.testwa.distest.server.web.task.validator;

import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.distest.server.mongo.model.AppiumRunningLog;
import com.testwa.distest.server.mongo.service.AppiumRunningLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 24/10/2017.
 */
@Component
public class StepValidatoer {

    @Autowired
    private AppiumRunningLogService procedureInfoService;


    public AppiumRunningLog validateProcedureExist(String entityId) throws ObjectNotExistsException {
        AppiumRunningLog entity = procedureInfoService.findOne(entityId);
        if(entity == null){
            throw new ObjectNotExistsException("步骤不存在");
        }
        return entity;
    }

}
