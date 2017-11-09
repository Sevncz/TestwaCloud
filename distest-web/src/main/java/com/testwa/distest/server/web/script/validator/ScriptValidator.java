package com.testwa.distest.server.web.script.validator;

import com.testwa.distest.common.exception.NoSuchScriptException;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.service.script.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wen on 23/10/2017.
 */
@Component
public class ScriptValidator {

    @Autowired
    private ScriptService scriptService;


    public List<Script> validateScriptsExist(List<Long> scriptIds) throws NoSuchScriptException {
        List<Script> scriptList = scriptService.findAll(scriptIds);
        if(scriptList == null || scriptList.size() != scriptIds.size()){
            throw new NoSuchScriptException("脚本不存在");
        }
        return scriptList;
    }

    public Script validateScriptExist(Long scriptId) throws NoSuchScriptException {
        Script entity = scriptService.findOne(scriptId);
        if(entity == null){
            throw new NoSuchScriptException("脚本不存在");
        }
        return entity;
    }


}
