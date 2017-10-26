package com.testwa.distest.server.websocket.service;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.testwa.core.Command;
import com.testwa.distest.server.websocket.WSFuncEnum;
import com.testwa.distest.server.web.auth.jwt.JwtTokenUtil;
import com.testwa.distest.server.mvc.service.DeviceService;
import com.testwa.distest.server.mvc.service.cache.RemoteClientService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    private DeviceService deviceService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

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
                    String userId = jwtTokenUtil.getUserIdFromToken(token);
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
                    Set<Object> subscribes = remoteClientService.getSubscribes(deviceId, func);
                    if(subscribes.size() == 1 && func.equals(WSFuncEnum.SCREEN.getValue())){
                        String sessionId = remoteClientService.getClientSessionByDeviceId(deviceId);
                        SocketIOClient agentClient = server.getClient(UUID.fromString(sessionId));
                        requestStartMinitouch(agentClient);
                        requestStartMinicap(agentClient);
                    }
                }
                TDevice td = deviceService.getDeviceById(deviceId);
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
            // 清理该客户端的缓存
            remoteClientService.delMainInfo(client.getSessionId().toString());
            try {
                String token = client.getHandshakeData().getSingleUrlParam("token");
                if(StringUtils.isNotBlank(token)){
                    String userId = jwtTokenUtil.getUserIdFromToken(token);
                    remoteClientService.userLogoutClient(userId);
                }
            } catch (Exception e) {
                log.error("parser token error", e);
            }
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


    private void requestStartMinitouch(SocketIOClient client){
        Map<String, Object> params = new HashMap<>();
        params.put("type", "minitouch");
        client.sendEvent(Command.Schem.START.getSchemString(), JSON.toJSONString(params));
    }

    private void requestStartMinicap(SocketIOClient client){
        Map<String, Object> params = new HashMap<>();
        params.put("type", "minicap");
        Map<String, Object> config = new HashMap<>();
//        config.put("rotate", 0.0f);
        config.put("scale", 0.25f);
        params.put("config", config);
        client.sendEvent(Command.Schem.START.getSchemString(), JSON.toJSONString(params));
    }

}
