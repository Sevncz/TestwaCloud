package com.testwa.distest.server.web.script.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.service.script.service.ScriptService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wen on 23/10/2017.
 */
@Component
public class ScriptValidator {

    @Autowired
    private ScriptService scriptService;


    public List<Script> validateScriptsExist(List<Long> scriptIds) {
        List<Script> scriptList = scriptService.findAll(scriptIds);
        if(scriptList == null || scriptList.size() != scriptIds.size()){
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        return scriptList;
    }
    public List<Script> validateScriptsInProject(List<Long> scriptIds, Long projectId) {
        List<Script> scriptList = scriptService.findAllInProject(scriptIds, projectId);
        Set<Long> scriptSet = new HashSet<>(scriptIds);
        if(scriptSet.size() != scriptList.size()){
            throw new BusinessException(ResultCode.CONFLICT, "非本项目中脚本无法使用");
        }
        return scriptList;
    }

    public Script validateScriptExist(Long scriptId) {
        Script entity = scriptService.findOne(scriptId);
        if(entity == null){
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        return entity;
    }

    public Script validateScriptInProject(Long scriptId, Long projectId) {
        Script entity = scriptService.findOneInPorject(scriptId, projectId);
        if(entity == null){
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        return entity;
    }


    public void validateScriptBelongApp(List<Long> scriptIds, String packageName) {
        List<Script> scriptList = scriptService.findAll(scriptIds);
        for(Script script : scriptList) {
            if(StringUtils.isNotBlank(script.getAppPackage())){
                if(!packageName.equals(script.getAppPackage())) {
                    throw new BusinessException(ResultCode.CONFLICT, "脚本和App不匹配");
                }
            }
        }
    }
}
