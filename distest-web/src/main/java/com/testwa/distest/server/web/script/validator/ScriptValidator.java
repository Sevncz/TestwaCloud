package com.testwa.distest.server.web.script.validator;

import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.service.script.service.ScriptService;
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


    public List<Script> validateScriptsExist(List<Long> scriptIds) throws ObjectNotExistsException {
        List<Script> scriptList = scriptService.findAll(scriptIds);
        if(scriptList == null || scriptList.size() != scriptIds.size()){
            throw new ObjectNotExistsException("脚本不存在");
        }
        return scriptList;
    }
    public List<Script> validateScriptsInProject(List<Long> scriptIds, Long projectId) throws ObjectNotExistsException {
        List<Script> scriptList = scriptService.findAllInProject(scriptIds, projectId);
        Set<Long> scriptSet = new HashSet<>();
        scriptSet.addAll(scriptIds);
        if(scriptSet.size() != scriptList.size()){
            throw new ObjectNotExistsException("脚本不存在");
        }
        return scriptList;
    }

    public Script validateScriptExist(Long scriptId) throws ObjectNotExistsException {
        Script entity = scriptService.findOne(scriptId);
        if(entity == null){
            throw new ObjectNotExistsException("脚本不存在");
        }
        return entity;
    }

    public Script validateScriptInProject(Long scriptId, Long projectId) throws ObjectNotExistsException {
        Script entity = scriptService.findOneInPorject(scriptId, projectId);
        if(entity == null){
            throw new ObjectNotExistsException("脚本不存在");
        }
        return entity;
    }


}
