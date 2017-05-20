package com.testwa.distest.client.web.startup;

import com.google.protobuf.ByteString;
import com.testwa.distest.client.boost.TestwaSocket;
import com.testwa.distest.client.model.AgentSystem;
import com.testwa.distest.client.rpc.client.LogcatClient;
import com.testwa.distest.client.rpc.proto.Agent;
import com.testwa.core.service.AdbDriverService;
import com.testwa.core.service.LogcatServiceBuilder;
import io.grpc.testwa.device.LogcatRequest;
import io.socket.client.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * Created by wen on 16/8/14.
 */
@Component
public class TestwaSocketSetup implements CommandLineRunner {
    private static Logger LOG = LoggerFactory.getLogger(TestwaSocketSetup.class);
    private final Socket socket;

    @Autowired
    private Environment env;

    @Autowired
    public TestwaSocketSetup(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run(String... strings) throws Exception {
        LOG.info("Ready start socket !!!!!!!");
        socket.on(Socket.EVENT_CONNECT, objects -> {
            LOG.info("websocket was connected");

            AgentSystem agentSystem = new AgentSystem();
            Agent.SystemInfo systemInfo = agentSystem.toAgentSystemInfo(env.getProperty("agent.key"));
            TestwaSocket.getSocket().emit("agentRegister", systemInfo.toByteArray());
        }).on(Socket.EVENT_DISCONNECT, objects -> LOG.info("websocket was disconnected"));
    }

}
