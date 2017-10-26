package com.testwa.distest.server.service.testcase.dao.impl;

import com.testwa.distest.common.dao.impl.BaseDAO;
import com.testwa.distest.server.mvc.entity.Script;
import com.testwa.distest.server.mvc.entity.Testcase;
import com.testwa.distest.server.mvc.mapper.ScriptMapper;
import com.testwa.distest.server.mvc.mapper.TestcaseMapper;
import com.testwa.distest.server.service.testcase.dao.ITestcaseDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class TestcaseDAO extends BaseDAO<Testcase, Long> implements ITestcaseDAO {

    @Resource
    private TestcaseMapper mapper;

    public List<Testcase> findBy(Testcase query) {
        return mapper.findBy(query);
    }

    @Override
    public long countBy(Testcase query) {

        return mapper.countBy(query);
    }

    @Override
    public List<Testcase> findAllOrder(List<Long> cases, String order) {
        return mapper.findAllOrder(cases, order);
    }


}