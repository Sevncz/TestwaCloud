package com.testwa.distest.server.web.script.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.entity.ScriptCase;
import com.testwa.distest.server.entity.ScriptCaseSet;
import com.testwa.distest.server.service.script.service.ScriptCaseService;
import com.testwa.distest.server.service.script.service.ScriptCaseSetService;
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
public class ScriptCaseSetValidator {

    @Autowired
    private ScriptCaseSetService scriptCaseSetService;

    public List<ScriptCaseSet> validateIdsExist(List<Long> scriptSetIds) {
        List<ScriptCaseSet> scriptList = scriptCaseSetService.listByIds(scriptSetIds);
        if (scriptList == null || scriptList.size() != scriptSetIds.size()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        return scriptList;
    }

    public ScriptCaseSet validateIdExist(Long scriptSetId) {
        ScriptCaseSet scriptSet = scriptCaseSetService.get(scriptSetId);
        if (scriptSet == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        return scriptSet;
    }

    public ScriptCaseSet validateScriptCaseSetIdExist(String scriptCaseSetId) {
        ScriptCaseSet scriptSet = scriptCaseSetService.getByScriptCaseSetId(scriptCaseSetId);
        if (scriptSet == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        return scriptSet;

    }
}
