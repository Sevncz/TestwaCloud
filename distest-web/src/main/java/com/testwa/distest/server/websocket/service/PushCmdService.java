package com.testwa.distest.server.websocket.service;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.common.enums.Command;
import com.testwa.core.cmd.MiniCmd;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.distest.server.service.cache.mgr.DeviceSessionMgr;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Log4j2
@Component
public class PushCmdService {

    @Autowired
    private DeviceSessionMgr deviceSessionMgr;

    private final SocketIOServer server;

    @Autowired
    public PushCmdService(SocketIOServer server) {
        this.server = server;
    }

    private SocketIOClient getSocketIOClient(String deviceId) throws ObjectNotExistsException {
        String sessionId = deviceSessionMgr.getDeviceSession(deviceId);
        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
        if(client == null){
            log.error("device session client not found");
            throw new ObjectNotExistsException("device session client not found");
        }
        return client;
    }

    @Async
    public void startTestcase(RemoteRunCommand cmd, String deviceId) throws ObjectNotExistsException {
        log.info(cmd.toString());
        SocketIOClient client = getSocketIOClient(deviceId);
        client.sendEvent(WebsocketEvent.ON_TESTCASE_RUN, JSON.toJSONString(cmd));
    }

    @Async
    public void pushMinCmdStart(MiniCmd cmd, String deviceId) throws ObjectNotExistsException {
        log.info(cmd.toString());
        SocketIOClient client = getSocketIOClient(deviceId);
        client.sendEvent(Command.Schem.START.getSchemString(), JSON.toJSONString(cmd));
    }

    public void pushTouchData(String deviceId, String data) throws ObjectNotExistsException {
        SocketIOClient client = getSocketIOClient(deviceId);
        client.sendEvent(Command.Schem.TOUCH.getSchemString(), data);
    }
}
