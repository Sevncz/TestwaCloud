package com.testwa.distest.server.api.websocket;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.testwa.core.Command;
import com.testwa.distest.server.api.WSFuncEnum;
import com.testwa.distest.server.model.TestwaDevice;
import com.testwa.distest.server.service.TestwaDeviceService;
import com.testwa.distest.server.service.cache.RemoteClientService;
import com.testwa.distest.server.service.security.TestwaTokenService;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wen on 16/9/5.
 */
@Component
public class WebConnectionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebConnectionHandler.class);

    private final SocketIOServer server;

    @Autowired
    private RemoteClientService remoteClientService;
    @Autowired
    private TestwaTokenService tokenService;
    @Autowired
    private TestwaDeviceService deviceService;

    @Autowired
    public WebConnectionHandler(SocketIOServer server) {
        this.server = server;
    }

    @OnConnect
    public void onConnect(SocketIOClient client) {

        String type = client.getHandshakeData().getSingleUrlParam("type");
        if("device".equals(type)){
            String serial = client.getHandshakeData().getSingleUrlParam("serial");
            Map<String, String> params = new HashMap<>();
            params.put("sn", serial);
            params.put("key", serial);
            // 设备连接
            client.sendEvent(Command.Schem.WAIT.getSchemString(), JSON.toJSONString(params));

            remoteClientService.saveDeviceForClient(client.getSessionId().toString(), serial);
        }else if("client".equals(type)){
            // 客户端连接
            try {
                String token = client.getHandshakeData().getSingleUrlParam("token");
                if(StringUtils.isNotBlank(token)){
                    Claims claims = tokenService.parserToken(token);
                    String userId = claims.getId();
                    remoteClientService.userLoginClient(userId, client.getSessionId().toString());
                }
            } catch (Exception e) {
                log.error("parser token error", e);
            }
        }else if("browser".equals(type)){
            // 浏览器连接, 订阅一个设备的输出流
            String func = client.getHandshakeData().getSingleUrlParam("func");
            String deviceId = client.getHandshakeData().getSingleUrlParam("deviceId");
            if(StringUtils.isNotBlank(func) && StringUtils.isNotBlank(deviceId) ){
                if(WSFuncEnum.contains(func)){
                    remoteClientService.subscribeDeviceEvent(deviceId, func, client.getSessionId().toString());
                }
                TestwaDevice td = deviceService.getDeviceById(deviceId);
                client.sendEvent("devices", JSON.toJSONString(td));
            }else{
                client.sendEvent("error", "参数不能为空");
            }

        }

    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {

        String type = client.getHandshakeData().getSingleUrlParam("type");
        if("device".equals(type)){
            // 设备连接断开
        }else if("client".equals(type)){
            // 客户端连接断开
        }else if("browser".equals(type)){
            // 浏览器连接断开
            log.debug("browser disconnect");
            String func = client.getHandshakeData().getSingleUrlParam("func");
            String deviceId = client.getHandshakeData().getSingleUrlParam("deviceId");
            if(StringUtils.isNotBlank(func) && StringUtils.isNotBlank(deviceId) ){
                if(WSFuncEnum.contains(func)){
                    remoteClientService.delSubscribe(deviceId, func, client.getSessionId().toString());
                }
            }else{
                client.sendEvent("error", "参数不能为空");
            }
        }
    }

}
