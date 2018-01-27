package com.testwa.distest.server.websocket.handler;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.common.enums.Command;
import com.testwa.distest.server.service.cache.mgr.DeviceSessionMgr;
import com.testwa.distest.server.websocket.service.PushCmdService;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 2016/9/24.
 */
@Slf4j
@Component
public class CommandHandler {

    private final static String minitouch = "minitouch";
    private final static String minicap = "minicap";
    private final static String open = "open";
    private final static String touch = "touch";

    @Autowired
    private DeviceSessionMgr deviceSessionMgr;
    @Autowired
    private PushCmdService pushCmdService;

    @OnEvent(value = minitouch)
    public void onMinitouch(SocketIOClient client, String data, AckRequest ackRequest) {
        if("open".equals(data)){
            log.info("minitouch open!");
        }
        if("close".equals(data)){
            log.info("minitouch close!");
        }
    }

    @OnEvent(value = minicap)
    public void onMinicap(SocketIOClient client, String data, AckRequest ackRequest) {
        if("open".equals(data)){
            log.info("onMinicap open!");
//            waitingData(client);
        }
        if("close".equals(data)){
            log.info("onMinicap close!");
        }
    }

    @OnEvent(value = open)
    public void onOpen(SocketIOClient client, String data, AckRequest ackRequest) {

        Object jsonObj = JSONObject.parse(data);

        String sn = ((JSONObject) jsonObj).getString("sn");
        log.info("Remote client {} open", sn);
    }

    @OnEvent(value = touch)
    public void onTouch(SocketIOClient client, String data, AckRequest ackRequest) throws ObjectNotExistsException {

        String func = client.getHandshakeData().getSingleUrlParam("func");
        String deviceId = client.getHandshakeData().getSingleUrlParam("deviceId");

        pushCmdService.pushTouchData(deviceId, data);

    }

    private void waitingData(SocketIOClient client){
        client.sendEvent(Command.Schem.WAITTING.getSchemString(), "");
    }


}
