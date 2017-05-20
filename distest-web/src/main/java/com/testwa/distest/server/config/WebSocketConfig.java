package com.testwa.distest.server.config;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by wen on 2016/12/10.
 */
@Configuration
public class WebSocketConfig {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(env.getProperty("wss.server.host"));
        config.setPort(Integer.parseInt(env.getProperty("wss.server.port")));
        config.setAuthorizationListener(handshakeData -> {
            String username = handshakeData.getSingleUrlParam("username");
            String password = handshakeData.getSingleUrlParam("password");
            log.info("websocket username: {}, password: {}", username, password);
            return true;
        });
        return new SocketIOServer(config);
    }

}
