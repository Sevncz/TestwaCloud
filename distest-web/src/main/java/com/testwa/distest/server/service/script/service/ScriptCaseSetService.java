package com.testwa.distest.server.service.script.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.service.BaseService;
import com.testwa.core.base.util.VoUtil;
import com.testwa.core.tools.SnowflakeIdWorker;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.entity.ScriptCase;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.script.form.ScriptCaseSetListForm;
import com.testwa.distest.server.entity.ScriptCaseSet;
import com.testwa.distest.server.mapper.ScriptCaseSetMapper;
import com.testwa.distest.server.service.script.form.ScriptCaseSetSaveForm;
import com.testwa.distest.server.service.script.form.ScriptCaseSetUpdateForm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ScriptCaseSetService extends BaseService<ScriptCaseSet, Long> {

    @Autowired
    private ScriptCaseSetMapper scriptCaseSetMapper;
    @Autowired
    private User currentUser;
    @Autowired
    private SnowflakeIdWorker commonIdWorker;

    public PageInfo<ScriptCaseSet> page(Long projectId, ScriptCaseSetListForm pageForm) {

        ScriptCaseSet query = new ScriptCaseSet();
        query.setProjectId(projectId);
        if(StringUtils.isNotBlank(pageForm.getCaseName())) {
            query.setCaseName(pageForm.getCaseName());
        }
        PageInfo<ScriptCaseSet> page = PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize())
                .setOrderBy(pageForm.getOrderBy() + " " + pageForm.getOrder())
                .doSelectPageInfo(()-> scriptCaseSetMapper.selectByCondition(query));
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(Project project, ScriptCaseSetSaveForm form) {
        ScriptCaseSet scriptCaseSet = VoUtil.buildVO(form, ScriptCaseSet.class);
        scriptCaseSet.setProjectId(project.getId());
        scriptCaseSet.setScriptCaseIds(JSON.toJSONString(form.getScriptCaseIds()));
        scriptCaseSet.setScriptCaseSetId(String.valueOf(commonIdWorker.nextId()));
        scriptCaseSetMapper.insert(scriptCaseSet);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Long[] entityIds) {
        Arrays.stream(entityIds).forEach(id -> {
            scriptCaseSetMapper.delete(id);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(ScriptCaseSetUpdateForm form) {
        ScriptCaseSet caseSet = VoUtil.buildVO(form, ScriptCaseSet.class);
        caseSet.setScriptCaseIds(JSON.toJSONString(form.getScriptCaseIds()));
        scriptCaseSetMapper.update(caseSet);
    }

    public List<ScriptCaseSet> listByIds(List<Long> scriptSetIds) {
        return scriptSetIds.stream().map(id -> scriptCaseSetMapper.selectById(id)).collect(Collectors.toList());
    }

    public ScriptCaseSet getByScriptCaseSetId(String scriptCaseSetId) {
        return scriptCaseSetMapper.selectByProperty(ScriptCaseSet::getScriptCaseSetId, scriptCaseSetId);
    }
}
