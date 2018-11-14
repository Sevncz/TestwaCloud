package com.testwa.distest.server.web.task.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.mongo.model.AppiumRunningLog;
import com.testwa.distest.server.mongo.model.Step;
import com.testwa.distest.server.mongo.service.AppiumRunningLogService;
import com.testwa.distest.server.mongo.service.StepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 24/10/2017.
 */
@Component
public class StepValidatoer {

    @Autowired
    private AppiumRunningLogService appiumRunningLogService;
    @Autowired
    private StepService stepService;


    public AppiumRunningLog validateAppiumLogExist(String entityId) {
        AppiumRunningLog entity = appiumRunningLogService.findOne(entityId);
        if(entity == null){
            throw new BusinessException(ResultCode.NOT_FOUND, "步骤不存在");
        }
        return entity;
    }

    public Step validateStepExist(String entityId) {
        Step entity = stepService.findOne(entityId);
        if(entity == null){
            throw new BusinessException(ResultCode.NOT_FOUND, "步骤不存在");
        }
        return entity;
    }

}
