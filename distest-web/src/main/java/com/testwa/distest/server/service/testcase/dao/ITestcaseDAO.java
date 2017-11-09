package com.testwa.distest.server.service.testcase.dao;

import com.testwa.distest.common.dao.IBaseDAO;
import com.testwa.distest.server.entity.Testcase;

import java.util.List;

/**
 * Created by wen on 19/10/2017.
 */
public interface ITestcaseDAO extends IBaseDAO<Testcase, Long> {
    List<Testcase> findBy(Testcase entity);

    long countBy(Testcase query);

    List<Testcase> findAllOrder(List<Long> cases, String order);
}
