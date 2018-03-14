package com.testwa.distest.server.web.task.validator;

import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.distest.server.mongo.model.ProcedureInfo;
import com.testwa.distest.server.mongo.service.ProcedureInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Component
public class StepValidatoer {

    @Autowired
    private ProcedureInfoService procedureInfoService;


    public ProcedureInfo validateProcedureExist(String entityId) throws ObjectNotExistsException {
        ProcedureInfo entity = procedureInfoService.findOne(entityId);
        if(entity == null){
            throw new ObjectNotExistsException("步骤不存在");
        }
        return entity;
    }

}
