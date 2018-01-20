package com.testwa.distest.server.service.testcase.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.TestcaseDetail;
import com.testwa.distest.server.mapper.TestcaseScriptMapper;
import com.testwa.distest.server.service.testcase.dao.ITestcaseDetailDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class TestcaseDetailDAO extends BaseDAO<TestcaseDetail, Long> implements ITestcaseDetailDAO {

    @Resource
    private TestcaseScriptMapper mapper;


    @Override
    public void deleteByTestcaseId(Long testcaseId) {
        mapper.deleteByTestcaseId(testcaseId);
    }
}