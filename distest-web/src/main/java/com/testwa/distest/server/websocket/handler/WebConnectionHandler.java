package com.testwa.distest.server.websocket.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.testwa.distest.config.security.JwtTokenUtil;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.cache.mgr.WebsocketLoginMgr;
import com.testwa.distest.server.service.cache.mgr.DeviceLoginMgr;
import com.testwa.distest.server.service.user.service.AgentLoginLoggerService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.mgr.DeviceOnlineMgr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 16/9/5.
 */
@Slf4j
@Component
public class WebConnectionHandler {

    @Autowired
    private WebsocketLoginMgr clientSessionMgr;
    @Autowired
    private DeviceLoginMgr deviceSessionMgr;
    @Autowired
    private DeviceOnlineMgr deviceOnlineMgr;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private AgentLoginLoggerService agentLoginLoggerService;

    @OnConnect
    public void onConnect(SocketIOClient client) {

        String type = client.getHandshakeData().getSingleUrlParam("type");
        if("device".equals(type)){

        }else if("client".equals(type)){
            // 客户端连接
            try {
                String token = client.getHandshakeData().getSingleUrlParam("token");
                if(StringUtils.isNotBlank(token)){
                    String username = jwtTokenUtil.getUsernameFromToken(token);
                    User user = userService.findByUsername(username);
                    clientSessionMgr.login(user.getId(), client.getSessionId().toString());
                }
            } catch (Exception e) {
                log.error("parser token error", e);
            }
        }else if("browser".equals(type)){
            // 浏览器连接, 订阅一个设备的图像输出流
        } else {
            log.error("Illegal connection");
        }
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {

        String type = client.getHandshakeData().getSingleUrlParam("type");
        if("device".equals(type)){
            // 设备连接断开
            String serial = client.getHandshakeData().getSingleUrlParam("serial");
            deviceSessionMgr.logout(serial);
            deviceOnlineMgr.offline(serial);

        }else if("client".equals(type)){
            // 客户端连接断开
            // 清理该客户端的缓存
//            clientSessionMgr.delMainInfo(client.getSessionId().toString());
            try {
                String token = client.getHandshakeData().getSingleUrlParam("token");
                if(StringUtils.isNotBlank(token)){
                    String username = jwtTokenUtil.getUsernameFromToken(token);
                    User user = userService.findByUsername(username);
                    clientSessionMgr.logout(user.getId());
                    agentLoginLoggerService.updateRecentLogoutTime(user.getUsername());
                }
            } catch (Exception e) {
                log.error("parser token error", e);
            }
        }else if("browser".equals(type)){
            // 浏览器连接断开
            log.debug("browser disconnect");
            // 清理资源

        }
    }


}
