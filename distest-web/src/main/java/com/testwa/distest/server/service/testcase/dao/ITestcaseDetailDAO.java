package com.testwa.distest.server.service.testcase.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.TestcaseDetail;

/**
 * Created by wen on 19/10/2017.
 */
public interface ITestcaseDetailDAO extends IBaseDAO<TestcaseDetail, Long> {
    void deleteByTestcaseId(Long testcaseId);
}
