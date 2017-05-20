package com.testwa.distest.client.config;

import com.testwa.distest.client.boost.TestwaApp;
import com.testwa.distest.client.boost.TestwaSocket;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.URISyntaxException;

/**
 * Created by wen on 16/9/11.
 */
@Configuration
public class WebsocketClientConfig {

    @Value("${agent.id}")
    private String agent_id;

    @Value("${agent.key}")
    private String agent_key;

    @Autowired
    private Environment env;

    @Bean
    public Socket socket() throws URISyntaxException {
        Socket socket = IO.socket(String.format("%s?username=%s&password=%s", env.getProperty("agent.socket.url"), env.getProperty("username"), env.getProperty("password")));
        TestwaSocket.setSocket(socket);
        return socket;
    }

    @Bean
    public TestwaApp app(){
        return new TestwaApp(agent_id, agent_key);
    }


}
