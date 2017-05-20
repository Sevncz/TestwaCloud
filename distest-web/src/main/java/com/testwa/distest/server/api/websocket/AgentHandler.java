package com.testwa.distest.server.api.websocket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.testwa.distest.client.rpc.proto.Agent;
import com.testwa.distest.server.model.TestwaAgent;
import com.testwa.distest.server.service.TestwaAgentService;
import com.testwa.distest.server.service.redis.TestwaAgentRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 2016/10/16.
 */
@Component
public class AgentHandler {
    private static final Logger log = LoggerFactory.getLogger(AgentHandler.class);

    private final SocketIOServer server;

    @Autowired
    private TestwaAgentRedisService agentRedisService;

    @Autowired
    private TestwaAgentService testwaAgentService;

    @Autowired
    private Environment env;

    @Autowired
    public AgentHandler(SocketIOServer server) {
        this.server = server;
    }

    @OnEvent(value = "agentRegister")
    public void onEvent(SocketIOClient client, byte[] data, AckRequest ackRequest) {
        try {
            Agent.SystemInfo systemInfo = Agent.SystemInfo.parseFrom(data);
            TestwaAgent agent = testwaAgentService.findTestwaAgentByMac(systemInfo.getMac());
            if(agent == null){
                TestwaAgent ta = new TestwaAgent();
                ta.toEntity(systemInfo);
                testwaAgentService.save(ta);
            }else{
                agent.toEntity(systemInfo);
                testwaAgentService.updateAgentInfo(agent);
            }
            agentRedisService.addClientSessionAgent(client.getSessionId().toString(), agent.getId());

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

}
