package com.testwa.distest.server.mvc.service;

import com.testwa.distest.server.mvc.model.Testcase;
import com.testwa.distest.server.mvc.repository.TestcaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/7.
 */
@Service
public class TestcaseService extends BaseService{

    @Autowired
    private TestcaseRepository testcaseRepository;

    public void save(Testcase testcase){
        testcaseRepository.save(testcase);
    }

    public void deleteById(String testcaseId){
        testcaseRepository.delete(testcaseId);
    }

    public Testcase getTestcaseById(String testcaseId){
        return testcaseRepository.findOne(testcaseId);
    }

    public Page<Testcase> findAll(PageRequest pageRequest) {
        return testcaseRepository.findAll(pageRequest);
    }

    public Page<Testcase> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return testcaseRepository.find(query, pageRequest);
    }
}
