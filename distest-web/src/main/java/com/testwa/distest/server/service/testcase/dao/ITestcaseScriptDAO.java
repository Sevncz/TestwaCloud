package com.testwa.distest.server.service.testcase.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.Testcase;
import com.testwa.distest.server.entity.TestcaseScript;

import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
public interface ITestcaseScriptDAO extends IBaseDAO<TestcaseScript, Long> {
    void deleteByTestcaseId(Long testcaseId);
}
