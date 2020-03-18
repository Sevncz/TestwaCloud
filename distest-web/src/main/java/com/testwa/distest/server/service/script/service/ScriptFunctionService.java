package com.testwa.distest.server.service.script.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.distest.server.entity.ScriptAction;
import com.testwa.distest.server.entity.ScriptFunction;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.ScriptActionMapper;
import com.testwa.distest.server.mapper.ScriptFunctionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * Created by wen on 09/03/2020.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ScriptFunctionService extends BaseService<ScriptFunction, Long> {
    @Autowired
    private ScriptFunctionMapper scriptFunctionMapper;
    @Autowired
    private User currentUser;

    @Transactional(rollbackFor = Exception.class)
    public ScriptFunction createFunction(Long projectId, String scriptCaseId, String fuuid, String title) {
        ScriptFunction scriptFunction = new ScriptFunction();
        scriptFunction.setProjectId(projectId);
        scriptFunction.setScriptCaseId(scriptCaseId);
        scriptFunction.setFunctionId(fuuid);
        scriptFunction.setTitle(title);
        scriptFunction.setCreateTime(new Date());
        scriptFunction.setCreateBy(currentUser.getId());
        scriptFunction.setUpdateTime(new Date());
        scriptFunction.setUpdateBy(currentUser.getId());
        scriptFunctionMapper.insert(scriptFunction);
        return scriptFunction;
    }

    public List<ScriptFunction> listByScriptCaseId(String scriptCaseId) {
        return scriptFunctionMapper.selectListByProperty(ScriptFunction::getScriptCaseId, scriptCaseId);
    }
}
