package com.testwa.distest.server.service;

import com.testwa.distest.server.model.Agent;
import com.testwa.distest.server.repository.AgentRepository;
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
public class AgentService extends BaseService {

    @Autowired
    private AgentRepository agentRepository;

    public void save(Agent agent){
        agentRepository.save(agent);
    }

    public void deleteById(String agentId){
        agentRepository.delete(agentId);
    }

    public Agent getTestwaAgentById(String agentId){
        return agentRepository.findOne(agentId);
    }

    public Page<Agent> findAll(PageRequest pageRequest) {
        return agentRepository.findAll(pageRequest);
    }

    public List<Agent> findAll() {
        return agentRepository.findAll();
    }

    public Agent findTestwaAgentByMac(String mac) {
        return agentRepository.findByMac(mac);
    }

    public void updateAgentInfo(Agent agent) {
        if(agent.getId() == null){
            return;
        }
        agentRepository.save(agent);
    }

    public Page<Agent> find(List<Map<String, String>> filters, PageRequest pageRequest) {
        Query query = buildQuery(filters);
        return agentRepository.find(query, pageRequest);
    }
}
