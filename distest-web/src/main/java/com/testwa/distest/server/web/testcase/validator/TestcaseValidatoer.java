package com.testwa.distest.server.web.testcase.validator;

import com.testwa.distest.common.exception.ObjectNotExistsException;
import com.testwa.distest.server.mvc.entity.Testcase;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Component
public class TestcaseValidatoer {

    @Autowired
    private TestcaseService testcaseService;


    public List<Testcase> validateTestcasesExist(List<Long> entityIds) throws ObjectNotExistsException {
        List<Testcase> entityList = testcaseService.findAll(entityIds);
        if(entityList == null || entityList.size() != entityIds.size()){
            throw new ObjectNotExistsException("任务不存在");
        }
        return entityList;
    }

    public Testcase validateTestcaseExist(Long entityId) throws ObjectNotExistsException {
        Testcase entity = testcaseService.findOne(entityId);
        if(entity == null){
            throw new ObjectNotExistsException("任务不存在");
        }
        return entity;
    }

}
