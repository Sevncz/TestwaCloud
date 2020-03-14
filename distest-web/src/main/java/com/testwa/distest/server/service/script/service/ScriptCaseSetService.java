package com.testwa.distest.server.service.script.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.service.BaseService;
import com.testwa.distest.server.service.script.form.ScriptCaseSetListForm;
import com.testwa.distest.server.entity.ScriptCaseSet;
import com.testwa.distest.server.mapper.ScriptCaseSetMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScriptCaseSetService extends BaseService<ScriptCaseSet, Long> {

    @Autowired
    private ScriptCaseSetMapper scriptCaseSetMapper;

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
}
