package com.testwa.distest.server.api.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.testwa.core.Command;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.client.rpc.proto.Agent;
import com.testwa.distest.server.model.TestwaReportSdetail;
import com.testwa.distest.server.service.TestwaReportSdetailService;
import com.testwa.distest.server.service.TestwaReportService;
import com.testwa.distest.server.service.cache.RemoteClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by wen on 2016/9/24.
 */


@Component
public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    private final SocketIOServer server;

    private final static String minitouch = "minitouch";
    private final static String minicap = "minicap";
    private final static String open = "open";
    private final static String touch = "touch";

    @Autowired
    private RemoteClientService remoteClientService;

    @Autowired
    public CommandHandler(SocketIOServer server) {
        this.server = server;
    }

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
        requestStartMinitouch(client);
        requestStartMinicap(client);
    }

    @OnEvent(value = touch)
    public void onTouch(SocketIOClient client, String data, AckRequest ackRequest) {

        String func = client.getHandshakeData().getSingleUrlParam("func");
        String deviceId = client.getHandshakeData().getSingleUrlParam("deviceId");
        String sessionId = remoteClientService.getDeviceForClient(deviceId);

        SocketIOClient deviceClient = server.getClient(UUID.fromString(sessionId));
        if(deviceClient != null){
            deviceClient.sendEvent(Command.Schem.TOUCH.getSchemString(), data);
        }else{
            log.error("device client is not found. {}", sessionId);
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
        config.put("scale", 0.3f);
        params.put("config", config);
        client.sendEvent(Command.Schem.START.getSchemString(), JSON.toJSONString(params));
    }


    private void waitingData(SocketIOClient client){
        client.sendEvent(Command.Schem.WAITTING.getSchemString(), "");
    }


}
