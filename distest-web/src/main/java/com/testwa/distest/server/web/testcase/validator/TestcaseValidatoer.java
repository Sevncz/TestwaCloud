package com.testwa.distest.server.web.testcase.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.Testcase;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wen on 24/10/2017.
 */
@Component
public class TestcaseValidatoer {

    @Autowired
    private TestcaseService testcaseService;


    public List<Testcase> validateTestcasesExist(List<Long> entityIds) {
        List<Testcase> entityList = testcaseService.findAll(entityIds);
        if(entityList == null || entityList.size() != entityIds.size()){
            throw new BusinessException(ResultCode.NOT_FOUND, "案例不存在");
        }
        return entityList;
    }
    public List<Testcase> validateTestcasesInProject(List<Long> entityIds, Long projectId) {
        List<Testcase> entityList = testcaseService.findAll(entityIds);

        Set<Long> entitySet = new HashSet<>(entityIds);
        if(entitySet.size() != entityList.size()){
            throw new BusinessException(ResultCode.NOT_FOUND, "案例不存在");
        }
        for(Testcase entity: entityList){
            if(!projectId.equals(entity.getProjectId())){
                throw new BusinessException(ResultCode.CONFLICT, "项目中不存在该案例");
            }
        }
        return entityList;
    }

    public Testcase validateTestcaseExist(Long entityId) {
        Testcase entity = testcaseService.get(entityId);
        if(entity == null){
            throw new BusinessException(ResultCode.NOT_FOUND, "案例不存在");
        }
        return entity;
    }

    public Testcase validateTestcaseInProject(Long entityId, Long projectId) {
        Testcase entity = testcaseService.get(entityId);
        if(entity == null){
            throw new BusinessException(ResultCode.NOT_FOUND, "案例不存在");
        }
        if(!projectId.equals(entity.getProjectId())){
            throw new BusinessException(ResultCode.CONFLICT, "项目中不存在该案例");
        }
        return entity;
    }

}
