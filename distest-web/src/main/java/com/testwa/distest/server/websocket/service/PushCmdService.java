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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private SocketIOClient getDeviceCLientSocketIOClient(String deviceId) throws ObjectNotExistsException {
        String sessionId = deviceSessionMgr.getDeviceSession(deviceId);
        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
        if(client == null){
            log.error("device session client not found");
            throw new ObjectNotExistsException("device session client not found");
        }
        return client;
    }
    private SocketIOClient getMainClientSocketIOClient(Long userId) throws ObjectNotExistsException {
        String sessionId = clientSessionMgr.getClientSession(userId);
        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
        if(client == null){
            log.error("client session not found");
            throw new ObjectNotExistsException("client session not found");
        }
        return client;
    }

    @Async
    public void pushMinCmdStart(MiniCmd cmd, String deviceId) throws ObjectNotExistsException {
        log.info(cmd.toString());
        SocketIOClient client = getDeviceCLientSocketIOClient(deviceId);
        client.sendEvent(Command.Schem.START.getSchemString(), JSON.toJSONString(cmd));
    }

    @Async
    public void pushTouchData(String deviceId, String data) throws ObjectNotExistsException {
        SocketIOClient client = getDeviceCLientSocketIOClient(deviceId);
        client.sendEvent(Command.Schem.TOUCH.getSchemString(), data);
    }

    /**
     * 安装并启动minicap和minitouch
     * @param userId
     * @param deviceId
     * @throws ObjectNotExistsException
     */
    @Async
    public void pushInitDeviceClient(Long userId, String deviceId) throws ObjectNotExistsException {
        SocketIOClient client = getMainClientSocketIOClient(userId);
        client.sendEvent(WebsocketEvent.ON_START, deviceId);
    }

    @Async
    public void executeCmd(RemoteRunCommand cmd, Long userId) throws ObjectNotExistsException {
        log.info(cmd.toString());
        SocketIOClient client = getMainClientSocketIOClient(userId);
        client.sendEvent(WebsocketEvent.ON_TESTCASE_RUN, JSON.toJSONString(cmd));
    }

}
