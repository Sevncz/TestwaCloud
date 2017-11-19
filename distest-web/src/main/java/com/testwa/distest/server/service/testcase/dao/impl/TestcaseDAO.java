package com.testwa.distest.server.service.testcase.dao.impl;

import com.testwa.distest.common.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.Testcase;
import com.testwa.distest.server.mapper.TestcaseMapper;
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

    @Override
    public Testcase findOne(Long key) {
        return mapper.findOne(key);
    }

    @Override
    public List<Testcase> findAll(List<Long> keys) {
        return mapper.findList(keys, null);
    }

    @Override
    public List<Testcase> findByFromProject(Map<String, Object> params) {
        return mapper.findByFromProject(params);
    }


}