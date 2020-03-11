package com.testwa.distest.server.service.script.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.service.BaseService;
import com.testwa.core.base.util.VoUtil;
import com.testwa.core.script.vo.ScriptActionVO;
import com.testwa.core.script.vo.ScriptCaseVO;
import com.testwa.core.script.vo.ScriptFunctionVO;
import com.testwa.core.tools.SnowflakeIdWorker;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.server.condition.ScriptCondition;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mapper.ScriptCaseMapper;
import com.testwa.distest.server.service.script.form.ScriptCaseListForm;
import com.testwa.distest.server.service.script.form.ScriptCaseSaveForm;
import com.testwa.distest.server.service.script.form.ScriptFunctionSaveForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by wen on 09/03/2020.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ScriptCaseService extends BaseService<ScriptCase, Long> {
    @Autowired
    private ScriptCaseMapper scriptCaseMapper;
    @Autowired
    private ScriptFunctionService scriptFunctionService;
    @Autowired
    private ScriptActionService scriptActionService;
    @Autowired
    private User currentUser;
    @Autowired
    private SnowflakeIdWorker commonIdWorker;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ScriptCase saveCase(Project project, ScriptCaseSaveForm form) {
        List<ScriptFunctionSaveForm> scriptFunctionSaveForms = form.getScriptFunctions();
        ScriptCase scriptCase = new ScriptCase();
        scriptCase.setScriptCaseName(form.getScriptName());
        scriptCase.setScriptCaseDesc(form.getScriptDesc());
        scriptCase.setProjectId(project.getId());
        scriptCase.setScriptCaseId(String.valueOf(commonIdWorker.nextId()));
        scriptCaseMapper.insert(scriptCase);
        List<String> functions = new ArrayList<>();
//        List<ScriptAction> actions = new ArrayList<>();

        scriptFunctionSaveForms.forEach(f -> {
            if(!functions.contains(f.getFunctionId())){
                functions.add(f.getFunctionId());
            }
        });
        // 创建方法
        Map<String, ScriptFunction> functionMap = new HashMap<>();
        functions.forEach(fuuid -> {
            ScriptFunction scriptFunction = scriptFunctionService.createFunction(project.getId(), scriptCase.getScriptCaseId(), fuuid);
            functionMap.put(fuuid, scriptFunction);
        });
        // 创建action
        scriptFunctionSaveForms.forEach(f -> {
            ScriptAction scriptAction = new ScriptAction();
            scriptAction.setAction(f.getAction());
            scriptAction.setFunctionId(f.getFunctionId());
            scriptAction.setParameter(JSON.toJSONString(f.getParameter()));
            scriptAction.setSeq(scriptFunctionSaveForms.indexOf(f));

            ScriptFunction function = functionMap.get(f.getFunctionId());
            scriptAction.setScriptCaseId(scriptCase.getScriptCaseId());
            scriptAction.setScriptFunctionId(function.getId());
            scriptAction.setCreateTime(new Date());
            scriptAction.setCreateBy(currentUser.getId());
            scriptAction.setUpdateTime(new Date());
            scriptAction.setUpdateBy(currentUser.getId());

            scriptActionService.insert(scriptAction);
        });
        return scriptCase;
    }

    public List<ScriptCase> list(Long projectId, ScriptCaseListForm queryForm) {
        PageHelper.orderBy(queryForm.getOrderBy() + " " + queryForm.getOrder());
        ScriptCondition query = new ScriptCondition();
        query.setProjectId(projectId);
        if(StringUtils.isNotBlank(queryForm.getScriptName())) {
            query.setScriptName(queryForm.getScriptName());
        }
        return scriptCaseMapper.selectByCondition(query);
    }

    public ScriptCase getByScriptCaseId(String scriptCaseId) {
        return scriptCaseMapper.selectByProperty(ScriptCase::getScriptCaseId, scriptCaseId);
    }

    public ScriptCaseVO getScriptCaseDetailVO(String scriptCaseId) {
        ScriptCase scriptCase = getByScriptCaseId(scriptCaseId);
        if(scriptCase == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        ScriptCaseVO vo = VoUtil.buildVO(scriptCase, ScriptCaseVO.class);
        List<ScriptFunction> scriptFunctions = scriptFunctionService.listByScriptCaseId(scriptCase.getScriptCaseId());
        List<ScriptFunctionVO> functionVOS = VoUtil.buildVOs(scriptFunctions, ScriptFunctionVO.class);
        vo.setFunctions(functionVOS);
        functionVOS.forEach(f -> {
            List<ScriptAction> actions = scriptActionService.listByFunctionId(f.getScriptCaseId(), f.getFunctionId());
            List<ScriptActionVO> actionVOS = VoUtil.buildVOs(actions, ScriptActionVO.class);
            f.setActions(actionVOS);
        });
        return vo;
    }

    public PageInfo<ScriptCase> page(Long projectId, ScriptCaseListForm pageForm) {
        ScriptCase query = new ScriptCase();
        query.setProjectId(projectId);
        if(StringUtils.isNotBlank(pageForm.getScriptName())) {
            query.setScriptCaseName(pageForm.getScriptName());
        }
        PageInfo<ScriptCase> page = new PageInfo<>();
        //分页处理
        page = PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize())
                .setOrderBy(pageForm.getOrderBy() + " " + pageForm.getOrder())
                .doSelectPageInfo(()-> scriptCaseMapper.selectByCondition(query));
        return page;
    }

    public List<ScriptCase> listAll(List<String> scriptCaseIds) {
        return scriptCaseIds.stream().map(this::getByScriptCaseId).collect(Collectors.toList());
    }
}
