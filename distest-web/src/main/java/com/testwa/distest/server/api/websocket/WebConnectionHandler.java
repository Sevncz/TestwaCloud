package com.testwa.distest.server.api.websocket;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.server.api.WSFlagEnum;
import com.testwa.distest.server.service.redis.TestwaAgentRedisService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by wen on 16/9/5.
 */
@Component
public class WebConnectionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebConnectionHandler.class);

    private final SocketIOServer server;
    private static String flag_key = "flag.%s.%s";

    @Autowired
    private StringRedisTemplate template;

    @Autowired
    private TestwaAgentRedisService agentRedisService;

    @Autowired
    public WebConnectionHandler(SocketIOServer server) {
        this.server = server;
    }

    @OnConnect
    public void onConnect(SocketIOClient client) {

        String flag = client.getHandshakeData().getSingleUrlParam("flag");
        String deviceId = client.getHandshakeData().getSingleUrlParam("deviceId");
        log.info("Connection, flag = {}, deviceId={}", flag, deviceId);

        if(StringUtils.isNotBlank(flag) && StringUtils.isNotBlank(deviceId) ){
            if(WSFlagEnum.contains(flag)){
                template.opsForValue().set(String.format(flag_key, flag, deviceId), client.getSessionId().toString());
            }
        }

        template.opsForSet().add(WebsocketEvent.CONNECT_SESSION, client.getSessionId().toString());
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        /**
         * 删除agent的连接信息
         */
        log.info("Disconnect");
        Set<String> deviceIds = template.opsForSet().members(String.format("client.session.devices.%s", client.getSessionId().toString()));

        // 清除设备和agent的关联
        for(String d : deviceIds){
            log.info("client disconnect, clear no used device, {}", d);
            template.opsForHash().delete(WebsocketEvent.DEVICE, d);
        }
        // 清除agent的session信息
        template.opsForSet().remove(WebsocketEvent.CONNECT_SESSION, client.getSessionId().toString());
        // 删除agent和device的关联
        template.delete(String.format("client.session.devices.%s", client.getSessionId().toString()));

        agentRedisService.delClientSessionAgent(client.getSessionId().toString());

        /**
         * 删除页面的连接信息
         */
        String flag = client.getHandshakeData().getSingleUrlParam("flag");
        String deviceId = client.getHandshakeData().getSingleUrlParam("deviceId");
        log.info("Connection, flag = {}, deviceId={}", flag, deviceId);
        if(StringUtils.isNotBlank(flag) && StringUtils.isNotBlank(deviceId) ){
            if(WSFlagEnum.contains(flag)){
                template.delete(String.format(flag_key, flag, deviceId));
            }
        }

    }

}
