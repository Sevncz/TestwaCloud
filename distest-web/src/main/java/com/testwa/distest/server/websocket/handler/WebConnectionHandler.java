package com.testwa.distest.server.websocket.handler;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.common.enums.Command;
import com.testwa.core.cmd.MiniCmd;
import com.testwa.distest.config.security.JwtTokenUtil;
import com.testwa.distest.server.entity.AgentLoginLogger;
import com.testwa.distest.server.entity.DeviceAndroid;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.cache.mgr.ClientSessionMgr;
import com.testwa.distest.server.service.cache.mgr.DeviceSessionMgr;
import com.testwa.distest.server.service.cache.mgr.SubscribeMgr;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.user.service.AgentLoginLoggerService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.websocket.WSFuncEnum;
import com.testwa.distest.server.websocket.service.PushCmdService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by wen on 16/9/5.
 */
@Slf4j
@Component
public class WebConnectionHandler {

    @Autowired
    private ClientSessionMgr clientSessionMgr;
    @Autowired
    private DeviceSessionMgr deviceSessionMgr;
    @Autowired
    private PushCmdService pushCmdService;
    @Autowired
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private SubscribeMgr subscribeMgr;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private AgentLoginLoggerService agentLoginLoggerService;

    @OnConnect
    @Async
    public void onConnect(SocketIOClient client) throws ObjectNotExistsException {

        String type = client.getHandshakeData().getSingleUrlParam("type");
        if("device".equals(type)){
            String serial = client.getHandshakeData().getSingleUrlParam("serial");
            Map<String, String> params = new HashMap<>();
            params.put("sn", serial);
            params.put("key", serial);
            // 设备连接
            client.sendEvent(Command.Schem.WAIT.getSchemString(), JSON.toJSONString(params));

            deviceSessionMgr.login(serial, client.getSessionId().toString());

            requestStartMinitouch(serial);
            requestStartMinicap(serial);
            requestStartStfService(serial);

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
            String func = client.getHandshakeData().getSingleUrlParam("func");
            String deviceId = client.getHandshakeData().getSingleUrlParam("deviceId");
            log.info("browser connected: {} - {}", func, deviceId);
            if(StringUtils.isNotBlank(func) && StringUtils.isNotBlank(deviceId) ){
                if(WSFuncEnum.contains(func)){
                    subscribeMgr.subscribeDeviceEvent(deviceId, func, client.getSessionId().toString());
                    if(subscribeMgr.isSubscribes(deviceId, func)){
                        pushCmdService.pushScreenUploadStart(deviceId);
                    }
                }
                DeviceAndroid deviceAndroid = (DeviceAndroid) deviceService.findByDeviceId(deviceId);
                client.sendEvent("devices", JSON.toJSON(deviceAndroid));
            }else{
                client.sendEvent("error", "参数不能为空");
            }
        }
    }

    @OnDisconnect
    @Async
    public void onDisconnect(SocketIOClient client) {

        String type = client.getHandshakeData().getSingleUrlParam("type");
        if("device".equals(type)){
            // 设备连接断开
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
            String func = client.getHandshakeData().getSingleUrlParam("func");
            String deviceId = client.getHandshakeData().getSingleUrlParam("deviceId");
            if(StringUtils.isNotBlank(func) && StringUtils.isNotBlank(deviceId) ){
                if(WSFuncEnum.contains(func)){
                    subscribeMgr.delSubscribe(deviceId, func, client.getSessionId().toString());
                    if(!subscribeMgr.isSubscribes(deviceId, func)){
                        pushCmdService.pushScreenUploadStop(deviceId);
                        pushCmdService.pushLogcatUploadStop(deviceId);
                    }
                }
            }else{
                client.sendEvent("error", "参数不能为空");
            }
        }
    }


    private void requestStartMinitouch(String deviceId) throws ObjectNotExistsException {
        MiniCmd cmd = new MiniCmd();
        cmd.setType("minitouch");
        pushCmdService.pushMinCmdStart(cmd, deviceId);
    }

    private void requestStartMinicap(String deviceId) throws ObjectNotExistsException {
        Map<String, Object> config = new HashMap<>();
//        config.put("rotate", 0.0f);
        config.put("scale", 0.25f);
        MiniCmd cmd = new MiniCmd();
        cmd.setType("minicap");
        cmd.setConfig(config);
        pushCmdService.pushMinCmdStart(cmd, deviceId);
    }
    private void requestStartStfService(String deviceId) throws ObjectNotExistsException {
        MiniCmd cmd = new MiniCmd();
        cmd.setType("stfagent");
        pushCmdService.pushStfAgentCmdStart(cmd, deviceId);
    }

}
