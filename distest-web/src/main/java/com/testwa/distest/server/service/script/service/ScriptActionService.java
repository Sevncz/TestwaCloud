package com.testwa.distest.server.service.script.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.distest.server.entity.ScriptAction;
import com.testwa.distest.server.mapper.ScriptActionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Created by wen on 09/03/2020.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ScriptActionService extends BaseService<ScriptAction, Long> {
    @Autowired
    private ScriptActionMapper scriptActionMapper;

    public List<ScriptAction> listByFunctionId(String scriptCaseId, String functionId) {
        ScriptAction query = new ScriptAction();
        query.setFunctionId(functionId);
        query.setScriptCaseId(scriptCaseId);
        return scriptActionMapper.selectByCondition(query);
    }
}
