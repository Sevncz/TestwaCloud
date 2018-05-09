package com.testwa.distest.server.websocket.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.protobuf.ByteString;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.cmd.MiniCmd;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.service.cache.mgr.DeviceSessionMgr;
import com.testwa.distest.server.service.cache.mgr.SubscribeDeviceFuncMgr;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.rpc.cache.CacheUtil;
import com.testwa.distest.server.websocket.WSFuncEnum;
import com.testwa.distest.server.websocket.service.PushCmdService;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.push.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Created by wen on 2016/9/24.
 */
@Slf4j
@Component
public class CommandHandler {

    private final static String minitouch = "minitouch";
    private final static String minicap = "minicap";
    private final static String resetMinicap = "reset_minicap";
//    private final static String open = "open";
    private final static String touch = "touch";
    private final static String input = "input";
    private final static String home = "home";
    private final static String back = "back";
    private final static String menu = "menu";
    private final static String del = "del";
    private final static String clear = "clear";
    private final static String getDevices = "get_devices";
    private final static String shell = "shell";
    private final static String web = "web";

    @Autowired
    private DeviceSessionMgr deviceSessionMgr;
    @Autowired
    private PushCmdService pushCmdService;
    @Autowired
    private SubscribeDeviceFuncMgr subscribeMgr;
    @Autowired
    private DeviceService deviceService;

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
        }
        if("close".equals(data)){
            log.info("onMinicap close!");
        }
    }

    @OnEvent(value = resetMinicap)
    public void onResetMinicap(SocketIOClient client, String data, AckRequest ackRequest) {

        Map params = JSON.parseObject(data, Map.class);
        String deviceId = (String) params.get("deviceId");
        if (isIllegalDeviceId(client, deviceId)) return;
        Object s = params.getOrDefault("scale", "0.5");
        Object r = params.getOrDefault("rotate", "0");
        Float scale = Float.parseFloat(s.toString());
        Float rotate = Float.parseFloat(r.toString());

        MiniCmd cmd = new MiniCmd();
        cmd.setType("minicap");
        cmd.setRotate(rotate);
        cmd.setScale(scale);
//        pushCmdService.pushMinCmdStart(cmd, deviceId);
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null){
            Message message = Message.newBuilder().setTopicName(Message.Topic.COMPONENT_STOP).setStatus("OK").setMessage(ByteString.copyFromUtf8("close minicap and minitouch")).build();
            observer.onNext(message);
            message = Message.newBuilder().setTopicName(Message.Topic.COMPONENT_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(cmd.getConfig()))).build();
            observer.onNext(message);
        }else{
            client.sendEvent("error", "设备还未准备好");
        }
    }

//    @OnEvent(value = open)
//    public void onOpen(SocketIOClient client, String data, AckRequest ackRequest) {
//
//        Object jsonObj = JSONObject.parse(data);
//
//        String deviceId = ((JSONObject) jsonObj).getString("sn");
//        log.info("Remote client {} open", deviceId);
//        Device deviceBase = deviceService.findByDeviceId(deviceId);
//        if(deviceBase != null){
//            deviceService.updateStatus(deviceId, DB.PhoneOnlineStatus.ONLINE);
//        }
//    }

    @OnEvent(value = touch)
    public void onTouch(SocketIOClient client, String data, AckRequest ackRequest) throws ObjectNotExistsException {

        Map params = JSON.parseObject(data, Map.class);
        String deviceId = (String) params.get("deviceId");
        if (isIllegalDeviceId(client, deviceId)) return;
        String touch = (String) params.get("touch");
        if(StringUtils.isBlank(touch)){
            client.sendEvent("error", "坐标不能为空");
            return;
        }
        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue(), client.getSessionId().toString())){
//            pushCmdService.pushTouchData(deviceId, touch);

            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
            if(observer != null ){
                Message message = Message.newBuilder().setTopicName(Message.Topic.TOUCH).setStatus("OK").setMessage(ByteString.copyFromUtf8(touch)).build();
                observer.onNext(message);
            }else{
                client.sendEvent("error", "设备还未准备好");
            }
        }

    }

    @OnEvent(value = input)
    public void onInput(SocketIOClient client, String data, AckRequest ackRequest) throws ObjectNotExistsException {
        Map params = JSON.parseObject(data, Map.class);
        String deviceId = (String) params.get("deviceId");
        if (isIllegalDeviceId(client, deviceId)) return;

        String input = (String) params.get("input");
        if(StringUtils.isBlank(input)){
            client.sendEvent("error", "请输入内容");
            return;
        }
        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue(), client.getSessionId().toString())){
//            pushCmdService.pushInputText(deviceId, input);

            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
            if(observer != null ){
                Message message = Message.newBuilder().setTopicName(Message.Topic.INPUT).setStatus("OK").setMessage(ByteString.copyFromUtf8(input)).build();
                observer.onNext(message);
            }else{
                client.sendEvent("error", "设备还未准备好");
            }
        }

    }

    @OnEvent(value = home)
    public void onHome(SocketIOClient client, String deviceId, AckRequest ackRequest) throws ObjectNotExistsException {
        if (isIllegalDeviceId(client, deviceId)) return;
        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue(), client.getSessionId().toString())) {
//            pushCmdService.pushHome(deviceId);

            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
            if(observer != null ){
                Message message = Message.newBuilder().setTopicName(Message.Topic.HOME).setStatus("OK").setMessage(ByteString.copyFromUtf8("home")).build();
                observer.onNext(message);
            }else{
                client.sendEvent("error", "设备还未准备好");
            }
        }

    }
    @OnEvent(value = back)
    public void onBack(SocketIOClient client, String deviceId, AckRequest ackRequest) throws ObjectNotExistsException {
        if (isIllegalDeviceId(client, deviceId)) return;
        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue(), client.getSessionId().toString())) {
//            pushCmdService.pushBack(deviceId);

            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
            if(observer != null ){
                Message message = Message.newBuilder().setTopicName(Message.Topic.BACK).setStatus("OK").setMessage(ByteString.copyFromUtf8("back")).build();
                observer.onNext(message);
            }else{
                client.sendEvent("error", "设备还未准备好");
            }
        }

    }

    @OnEvent(value = menu)
    public void onMenu(SocketIOClient client, String deviceId, AckRequest ackRequest) throws ObjectNotExistsException {
        if (isIllegalDeviceId(client, deviceId)) return;
        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue(), client.getSessionId().toString())) {
//            pushCmdService.pushMenu(deviceId);
            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
            if(observer != null ){
                Message message = Message.newBuilder().setTopicName(Message.Topic.MENU).setStatus("OK").setMessage(ByteString.copyFromUtf8("menu")).build();
                observer.onNext(message);
            }else{
                client.sendEvent("error", "设备还未准备好");
            }
        }

    }

    @OnEvent(value = del)
    public void onDel(SocketIOClient client, String deviceId, AckRequest ackRequest) throws ObjectNotExistsException {
        if (isIllegalDeviceId(client, deviceId)) return;
        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue(), client.getSessionId().toString())) {
//            pushCmdService.pushDel(deviceId);
            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
            if(observer != null ){
                Message message = Message.newBuilder().setTopicName(Message.Topic.DEL).setStatus("OK").setMessage(ByteString.copyFromUtf8("delete")).build();
                observer.onNext(message);
            }else{
                client.sendEvent("error", "设备还未准备好");
            }
        }
    }

    @OnEvent(value = getDevices)
    public void onGetDevices(SocketIOClient client, String deviceId, AckRequest ackRequest) throws ObjectNotExistsException {
        if (isIllegalDeviceId(client, deviceId)) return;
        Device deviceAndroid = deviceService.findByDeviceId(deviceId);
        client.sendEvent("devices", JSON.toJSON(deviceAndroid));
    }

    private boolean isIllegalDeviceId(SocketIOClient client, String deviceId) {
        if(StringUtils.isBlank(deviceId)){
            client.sendEvent("error", "deviceId不能为空");
            return true;
        }
        return false;
    }

    @OnEvent(value = shell)
    public void onShell(SocketIOClient client, String data, AckRequest ackRequest) throws ObjectNotExistsException {
        Map params = JSON.parseObject(data, Map.class);
        String deviceId = (String) params.get("deviceId");
        if (isIllegalDeviceId(client, deviceId)) return;

        String cmd = (String) params.get("cmd");
        if(StringUtils.isBlank(cmd)){
            client.sendEvent("error", "shell命令不能为空");
            return;
        }
//        pushCmdService.pushShell(deviceId, cmd);
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            Message message = Message.newBuilder().setTopicName(Message.Topic.SHELL).setStatus("OK").setMessage(ByteString.copyFromUtf8(data)).build();
            observer.onNext(message);
        }else{
            client.sendEvent("error", "设备还未准备好");
        }
    }

    @OnEvent(value = web)
    public void onWeb(SocketIOClient client, String data, AckRequest ackRequest) throws ObjectNotExistsException {
        Map params = JSON.parseObject(data, Map.class);
        String deviceId = (String) params.get("deviceId");
        if (isIllegalDeviceId(client, deviceId)) return;

        String url = (String) params.get("url");
        if(StringUtils.isBlank(url)){
            client.sendEvent("error", "网址不能为空");
            return;
        }
//        pushCmdService.pushOpenWeb(deviceId, url);
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            Message message = Message.newBuilder().setTopicName(Message.Topic.OPENWEB).setStatus("OK").setMessage(ByteString.copyFromUtf8(url)).build();
            observer.onNext(message);
        }else{
            client.sendEvent("error", "设备还未准备好");
        }
    }

}
