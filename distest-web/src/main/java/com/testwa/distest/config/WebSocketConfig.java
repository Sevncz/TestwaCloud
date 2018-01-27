package com.testwa.distest.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.distest.config.security.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by wen on 2016/12/10.
 */
@Slf4j
@Configuration
public class WebSocketConfig {
    @Autowired
    private Environment env;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(env.getProperty("wss.server.host"));
        config.setPort(Integer.parseInt(env.getProperty("wss.server.port")));
        config.setAuthorizationListener(handshakeData -> {
            String token = handshakeData.getSingleUrlParam("token");
            log.info("websocket token: {}, url: {}", token, handshakeData.getUrl());
            String username = jwtTokenUtil.getUsernameFromToken(token);
//            return StringUtils.isNotEmpty(username);
            return true;
        });
        return new SocketIOServer(config);
    }

}
