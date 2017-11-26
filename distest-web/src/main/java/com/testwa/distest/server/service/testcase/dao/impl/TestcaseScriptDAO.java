package com.testwa.distest.server.service.testcase.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.TestcaseScript;
import com.testwa.distest.server.mapper.TestcaseScriptMapper;
import com.testwa.distest.server.service.testcase.dao.ITestcaseScriptDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class TestcaseScriptDAO extends BaseDAO<TestcaseScript, Long> implements ITestcaseScriptDAO {

    @Resource
    private TestcaseScriptMapper mapper;


    @Override
    public void deleteByTestcaseId(Long testcaseId) {
        mapper.deleteByTestcaseId(testcaseId);
    }
}