package com.testwa.distest.server.websocket.handler;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.testwa.core.common.enums.Command;
import com.testwa.core.entity.DeviceBase;
import com.testwa.core.entity.transfer.MiniCmd;
import com.testwa.distest.server.service.cache.mgr.ClientSessionMgr;
import com.testwa.distest.server.service.cache.mgr.DeviceSessionMgr;
import com.testwa.distest.server.service.cache.mgr.SubscribeMgr;
import com.testwa.distest.server.service.cache.mgr.DeviceCacheMgr;
import com.testwa.distest.server.websocket.WSFuncEnum;
import com.testwa.distest.server.web.auth.jwt.JwtTokenUtil;
import com.testwa.distest.server.websocket.service.PushCmdService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by wen on 16/9/5.
 */
@Component
public class WebConnectionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebConnectionHandler.class);

    @Autowired
    private ClientSessionMgr clientSessionMgr;
    @Autowired
    private DeviceSessionMgr deviceSessionMgr;
    @Autowired
    private PushCmdService pushCmdService;
    @Autowired
    private DeviceCacheMgr deviceCacheMgr;
    @Autowired
    private SubscribeMgr subscribeMgr;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

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

            deviceSessionMgr.login(serial, client.getSessionId().toString());
        }else if("client".equals(type)){
            // 客户端连接
            try {
                String token = client.getHandshakeData().getSingleUrlParam("token");
                if(StringUtils.isNotBlank(token)){
                    Long userId = jwtTokenUtil.getUserIdFromToken(token);
                    clientSessionMgr.login(userId, client.getSessionId().toString());
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
                    subscribeMgr.subscribeDeviceEvent(deviceId, func, client.getSessionId().toString());
                    Set<String> subscribes = subscribeMgr.getSubscribes(deviceId, func);
                    if(subscribes.size() == 1 && func.equals(WSFuncEnum.SCREEN.getValue())){
                        requestStartMinitouch(deviceId);
                        requestStartMinicap(deviceId);
                    }
                }
                DeviceBase td = deviceCacheMgr.getDeviceContent(deviceId);
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
//            clientSessionMgr.delMainInfo(client.getSessionId().toString());
            try {
                String token = client.getHandshakeData().getSingleUrlParam("token");
                if(StringUtils.isNotBlank(token)){
                    Long userId = jwtTokenUtil.getUserIdFromToken(token);
                    clientSessionMgr.logout(userId);
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
                }
            }else{
                client.sendEvent("error", "参数不能为空");
            }
        }
    }


    private void requestStartMinitouch(String deviceId){
        MiniCmd cmd = new MiniCmd();
        cmd.setType("minitouch");
        pushCmdService.pushMinCmdStart(cmd, deviceId);
    }

    private void requestStartMinicap(String deviceId){
        Map<String, Object> config = new HashMap<>();
//        config.put("rotate", 0.0f);
        config.put("scale", 0.25f);
        MiniCmd cmd = new MiniCmd();
        cmd.setType("minicap");
        cmd.setConfig(config);
        pushCmdService.pushMinCmdStart(cmd, deviceId);
    }

}
