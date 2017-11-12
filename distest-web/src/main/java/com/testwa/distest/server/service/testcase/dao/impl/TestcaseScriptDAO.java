package com.testwa.distest.server.service.testcase.dao.impl;

import com.testwa.distest.common.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.Testcase;
import com.testwa.distest.server.entity.TestcaseScript;
import com.testwa.distest.server.mvc.mapper.TestcaseMapper;
import com.testwa.distest.server.mvc.mapper.TestcaseScriptMapper;
import com.testwa.distest.server.service.testcase.dao.ITestcaseDAO;
import com.testwa.distest.server.service.testcase.dao.ITestcaseScriptDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class TestcaseScriptDAO extends BaseDAO<TestcaseScript, Long> implements ITestcaseScriptDAO {

    @Resource
    private TestcaseScriptMapper mapper;


    @Override
    public void deleteByTestcaseId(Long testcaseId) {
        mapper.deleteByTestcaseId(testcaseId);
    }
}