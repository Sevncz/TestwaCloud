package com.testwa.distest.server.service;

import com.testwa.distest.server.model.TestwaAgent;
import com.testwa.distest.server.model.TestwaApp;
import com.testwa.distest.server.repository.TestwaAgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by wen on 16/9/1.
 */
@Service
public class TestwaAgentService extends BaseService {

    @Autowired
    private TestwaAgentRepository agentRepository;

    public void save(TestwaAgent testwaAgent){
        agentRepository.save(testwaAgent);
    }

    public void deleteById(String agentId){
        agentRepository.delete(agentId);
    }

    public TestwaAgent getTestwaAgentById(String agentId){
        return agentRepository.findOne(agentId);
    }

    public Page<TestwaAgent> findAll(PageRequest pageRequest) {
        return agentRepository.findAll(pageRequest);
    }

    public List<TestwaAgent> findAll() {
        return agentRepository.findAll();
    }

    public TestwaAgent findTestwaAgentByMac(String mac) {
        return agentRepository.findByMac(mac);
    }

    public void updateAgentInfo(TestwaAgent agent) {
        if(agent.getId() == null){
            return;
        }
        agentRepository.save(agent);
    }

    public Page<TestwaAgent> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return agentRepository.find(query, pageRequest);
    }
}
