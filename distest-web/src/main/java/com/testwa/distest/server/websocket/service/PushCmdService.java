package com.testwa.distest.server.websocket.service;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.common.enums.Command;
import com.testwa.core.entity.transfer.MiniCmd;
import com.testwa.core.entity.transfer.RemoteRunCommand;
import com.testwa.distest.server.service.cache.mgr.DeviceSessionMgr;
import com.testwa.distest.server.websocket.handler.WebConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PushCmdService {
    private static final Logger log = LoggerFactory.getLogger(PushCmdService.class);

    @Autowired
    private DeviceSessionMgr deviceSessionMgr;

    private final SocketIOServer server;

    @Autowired
    public PushCmdService(SocketIOServer server) {
        this.server = server;
    }

    private SocketIOClient getSocketIOClient(String deviceId) {
        String sessionId = deviceSessionMgr.getClientSessionId(deviceId);
        return server.getClient(UUID.fromString(sessionId));
    }

    @Async
    public void startTestcase(RemoteRunCommand cmd, String deviceId){
        log.info(cmd.toString());
        SocketIOClient client = getSocketIOClient(deviceId);
        client.sendEvent(WebsocketEvent.ON_TESTCASE_RUN, JSON.toJSONString(cmd));
    }

    @Async
    public void pushMinCmdStart(MiniCmd cmd, String deviceId){
        log.info(cmd.toString());
        SocketIOClient client = getSocketIOClient(deviceId);
        client.sendEvent(Command.Schem.START.getSchemString(), JSON.toJSONString(cmd));
    }

}
