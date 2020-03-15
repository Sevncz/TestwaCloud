package com.testwa.distest.server.web.script.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.entity.ScriptCase;
import com.testwa.distest.server.service.script.service.ScriptCaseService;
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
    @Autowired
    private ScriptCaseService scriptCaseService;

    public List<Script> validateScriptsExist(List<Long> scriptIds) {
        List<Script> scriptList = scriptService.findAll(scriptIds);
        if (scriptList == null || scriptList.size() != scriptIds.size()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        return scriptList;
    }

    public List<ScriptCase> validateScriptCasesExist(List<String> scriptCaseIds) {
        List<ScriptCase> scriptList = scriptCaseService.listByScriptCaseId(scriptCaseIds);
        if (scriptList == null || scriptList.size() != scriptCaseIds.size()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        return scriptList;
    }

    public List<Script> validateScriptsInProject(List<Long> scriptIds, Long projectId) {
        List<Script> scriptList = scriptService.findAllInProject(scriptIds, projectId);
        Set<Long> scriptSet = new HashSet<>(scriptIds);
        if (scriptSet.size() != scriptList.size()) {
            throw new BusinessException(ResultCode.CONFLICT, "非本项目中脚本无法使用");
        }
        return scriptList;
    }

    public Script validateScriptExist(Long scriptId) {
        Script entity = scriptService.findOne(scriptId);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        return entity;
    }

    public ScriptCase validateScriptCaseExist(String scriptId) {
        ScriptCase entity = scriptCaseService.getByScriptCaseId(scriptId);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        return entity;
    }

    public Script validateScriptInProject(Long scriptId, Long projectId) {
        Script entity = scriptService.findOneInPorject(scriptId, projectId);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        return entity;
    }


    public void validateScriptBelongApp(List<Long> scriptIds, String packageName) {
        List<Script> scriptList = scriptService.findAll(scriptIds);
        for (Script script : scriptList) {
            if (StringUtils.isNotBlank(script.getAppPackage())) {
                if (!packageName.equals(script.getAppPackage())) {
                    throw new BusinessException(ResultCode.CONFLICT, "脚本和App不匹配");
                }
            }
        }
    }

    public void validateScriptCaseBelongApp(List<String> scriptcaseIds, String packageName) {
        List<ScriptCase> scriptCaseList = scriptCaseService.listByScriptCaseId(scriptcaseIds);
        for (ScriptCase script : scriptCaseList) {
            if (StringUtils.isNotBlank(script.getAppBasePackage())) {
                if (!packageName.equals(script.getAppBasePackage())) {
                    throw new BusinessException(ResultCode.CONFLICT, "脚本和App不匹配");
                }
            }
        }
    }

    public void validateScriptCaseBelongApp(String scriptcaseId, String packageName) {
        ScriptCase scriptCase = scriptCaseService.getByScriptCaseId(scriptcaseId);
        if (StringUtils.isNotBlank(scriptCase.getAppBasePackage())) {
            if (!packageName.equals(scriptCase.getAppBasePackage())) {
                throw new BusinessException(ResultCode.CONFLICT, "脚本和App不匹配");
            }
        }
    }
}
