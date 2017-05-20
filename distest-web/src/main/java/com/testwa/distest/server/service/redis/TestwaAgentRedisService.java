package com.testwa.distest.server.service.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by wen on 2016/10/16.
 */
@Service
public class TestwaAgentRedisService {
    private static final Logger log = LoggerFactory.getLogger(TestwaDeviceRedisService.class);
    private static String clientSessionAgentKey = "client.session.agent.%s";

    @Autowired
    private StringRedisTemplate template;


    public void addClientSessionAgent(String sessionId, String agentId){
        /**
         * 保存session对应的device, one to many
         */
        template.opsForValue().set(String.format(clientSessionAgentKey, sessionId), agentId);
    }

    public void delClientSessionAgent(String sessionId){
        /**
         * 保存session对应的device, one to many
         */
        template.delete(String.format(clientSessionAgentKey, sessionId));
    }

    public String getAgentBySessionId(String sessionId) {
        return template.opsForValue().get(String.format(clientSessionAgentKey, sessionId));
    }
}
