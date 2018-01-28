package com.testwa.distest.server.websocket.service;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.common.enums.Command;
import com.testwa.core.cmd.MiniCmd;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.distest.server.service.cache.mgr.ClientSessionMgr;
import com.testwa.distest.server.service.cache.mgr.DeviceSessionMgr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class PushCmdService {

    @Autowired
    private DeviceSessionMgr deviceSessionMgr;
    @Autowired
    private ClientSessionMgr clientSessionMgr;

    private final SocketIOServer server;

    @Autowired
    public PushCmdService(SocketIOServer server) {
        this.server = server;
    }

    private SocketIOClient getDeviceClientSocketIOClient(String deviceId) {
        String sessionId = deviceSessionMgr.getDeviceSession(deviceId);
        if(StringUtils.isEmpty(sessionId)){
            log.error("device {} session not found", deviceId);
            return null;
        }
        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
        if(client == null){
            log.error("device {} SocketIOClient not found", deviceId);
            return null;
        }
        return client;
    }
    private SocketIOClient getMainClientSocketIOClient(Long userId) {
        String sessionId = clientSessionMgr.getClientSession(userId);
        if(StringUtils.isEmpty(sessionId)){
            log.error("agent {} session not found", userId);
            return null;
        }
        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
        if(client == null){
            log.error("agent {} SocketIOClient not found", userId);
            return null;
        }
        return client;
    }

    @Async
    public void pushMinCmdStart(MiniCmd cmd, String deviceId) {
        log.info(cmd.toString());
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
        client.sendEvent(Command.Schem.START.getSchemString(), JSON.toJSONString(cmd));
    }

    @Async
    public void pushTouchData(String deviceId, String data) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
        client.sendEvent(Command.Schem.TOUCH.getSchemString(), data);
    }

    /**
     * 安装并启动minicap和minitouch
     * @param userId
     * @param deviceId
     * @throws ObjectNotExistsException
     */
    @Async
    public void pushInitDeviceClient(Long userId, String deviceId) {
        SocketIOClient client = getMainClientSocketIOClient(userId);
        if(client != null)
        client.sendEvent(WebsocketEvent.ON_START, deviceId);
    }

    /**
     * 屏幕截图上传开始
     * @param deviceId
     */
    public void pushScreenUploadStart(String deviceId) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
        client.sendEvent(Command.Schem.WAITTING.getSchemString(), "");
    }
    /**
     * 屏幕截图上传停止
     * @param deviceId
     */
    public void pushScreenUploadStop(String deviceId) {
        SocketIOClient client = getDeviceClientSocketIOClient(deviceId);
        if(client != null)
        client.sendEvent(Command.Schem.WAIT.getSchemString(), "");
    }

    @Async
    public void executeCmd(RemoteRunCommand cmd, Long userId) {
        log.info(cmd.toString());
        SocketIOClient client = getMainClientSocketIOClient(userId);
        if(client != null)
        client.sendEvent(WebsocketEvent.ON_TESTCASE_RUN, JSON.toJSONString(cmd));
    }
}
