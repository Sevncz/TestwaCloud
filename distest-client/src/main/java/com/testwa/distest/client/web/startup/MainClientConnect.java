package com.testwa.distest.client.web.startup;

import com.testwa.distest.client.control.client.MainClient;
import com.testwa.distest.client.model.AgentSystem;
import com.testwa.distest.client.rpc.proto.Agent;
import io.socket.client.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 16/8/14.
 */
@Component
public class MainClientConnect implements CommandLineRunner {
    private static Logger log = LoggerFactory.getLogger(MainClientConnect.class);
    private final Socket ws;

    @Autowired
    public MainClientConnect(Socket ws) {
        this.ws = ws;
    }

    @Autowired
    private Environment env;

    @Override
    public void run(String... strings) throws Exception {
        log.info("Socket connecting");
        ws.on(Socket.EVENT_CONNECT, objects -> {
            log.info("websocket was connected");

            AgentSystem agentSystem = new AgentSystem();
            Agent.SystemInfo systemInfo = agentSystem.toAgentSystemInfo(env.getProperty("agent.key"));
            MainClient.getWs().emit("agentRegister", systemInfo.toByteArray());
        }).on(Socket.EVENT_DISCONNECT, objects -> log.info("websocket was disconnected"));
        ws.connect();

        log.info("hahhhahhhahahh h");
    }
}
