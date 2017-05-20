package com.testwa.distest.server.service;

import com.testwa.distest.server.model.TestwaScript;
import com.testwa.distest.server.model.TestwaTestcase;
import com.testwa.distest.server.repository.TestwaTestcaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class TestwaTestcaseService extends BaseService{

    @Autowired
    private TestwaTestcaseRepository testwaTestcaseRepository;

    public void save(TestwaTestcase testcase){
        testwaTestcaseRepository.save(testcase);
    }

    public void deleteById(String testcaseId){
        testwaTestcaseRepository.delete(testcaseId);
    }

    public TestwaTestcase getTestcaseById(String testcaseId){
        return testwaTestcaseRepository.findOne(testcaseId);
    }

    public Page<TestwaTestcase> findAll(PageRequest pageRequest) {
        return testwaTestcaseRepository.findAll(pageRequest);
    }

    public Page<TestwaTestcase> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return testwaTestcaseRepository.find(query, pageRequest);
    }
}
