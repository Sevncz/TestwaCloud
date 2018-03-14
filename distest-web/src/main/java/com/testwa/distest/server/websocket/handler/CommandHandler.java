package com.testwa.distest.server.websocket.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.distest.server.service.cache.mgr.DeviceSessionMgr;
import com.testwa.distest.server.service.cache.mgr.SubscribeMgr;
import com.testwa.distest.server.websocket.WSFuncEnum;
import com.testwa.distest.server.websocket.service.PushCmdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

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
    private final static String input = "input";
    private final static String home = "home";
    private final static String back = "back";
    private final static String menu = "menu";
    private final static String del = "del";
    private final static String clear = "clear";

    @Autowired
    private DeviceSessionMgr deviceSessionMgr;
    @Autowired
    private PushCmdService pushCmdService;
    @Autowired
    private SubscribeMgr subscribeMgr;

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

        Map params = JSON.parseObject(data, Map.class);
        String deviceId = (String) params.get("deviceId");
        String touch = (String) params.get("touch");
        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue())){
            pushCmdService.pushTouchData(deviceId, touch);
        }

    }

    @OnEvent(value = input)
    public void onInput(SocketIOClient client, String data, AckRequest ackRequest) throws ObjectNotExistsException {
        Map params = JSON.parseObject(data, Map.class);
        String deviceId = (String) params.get("deviceId");
        String input = (String) params.get("input");
        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue())){
            pushCmdService.pushInputText(deviceId, input);
        }

    }

    @OnEvent(value = home)
    public void onHome(SocketIOClient client, String deviceId, AckRequest ackRequest) throws ObjectNotExistsException {

        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue())) {
            pushCmdService.pushHome(deviceId);
        }

    }
    @OnEvent(value = back)
    public void onBack(SocketIOClient client, String deviceId, AckRequest ackRequest) throws ObjectNotExistsException {

        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue())) {
            pushCmdService.pushBack(deviceId);
        }

    }

    @OnEvent(value = menu)
    public void onMenu(SocketIOClient client, String deviceId, AckRequest ackRequest) throws ObjectNotExistsException {

        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue())) {
            pushCmdService.pushMenu(deviceId);
        }

    }

    @OnEvent(value = del)
    public void onDel(SocketIOClient client, String deviceId, AckRequest ackRequest) throws ObjectNotExistsException {

        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue())) {
            pushCmdService.pushDel(deviceId);
        }
    }

}
