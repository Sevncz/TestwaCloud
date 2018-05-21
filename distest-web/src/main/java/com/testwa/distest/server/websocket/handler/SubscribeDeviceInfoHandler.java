package com.testwa.distest.server.websocket.handler;import com.alibaba.fastjson.JSON;import com.corundumstudio.socketio.AckRequest;import com.corundumstudio.socketio.SocketIOClient;import com.corundumstudio.socketio.annotation.OnEvent;import com.google.protobuf.ByteString;import com.testwa.core.base.exception.ObjectNotExistsException;import com.testwa.core.cmd.MiniCmd;import com.testwa.distest.server.entity.Device;import com.testwa.distest.server.service.cache.mgr.SubscribeDeviceFuncMgr;import com.testwa.distest.server.service.device.service.DeviceService;import com.testwa.distest.server.service.rpc.cache.CacheUtil;import com.testwa.distest.server.websocket.WSFuncEnum;import com.testwa.distest.server.websocket.service.PushCmdService;import io.grpc.stub.StreamObserver;import io.rpc.testwa.push.Message;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Component;import java.util.HashMap;import java.util.Map;@Slf4j@Componentpublic class SubscribeDeviceInfoHandler {    private final static String SUB_SCREEN = "sub_screen";    private final static String WAIT_SCREEN = "wait_screen";    private final static String SUB_LOGCAT = "sub_logcat";    private final static String WAIT_LOGCAT = "wait_logcat";    @Autowired    private DeviceService deviceService;    @Autowired    private PushCmdService pushCmdService;    @Autowired    private SubscribeDeviceFuncMgr subscribeMgr;    /**     *@Description: 订阅设备屏幕     *@Param: [client, deviceId, ackRequest]     *@Return: void     *@Author: wen     *@Date: 2018/4/28     */    @OnEvent(value = SUB_SCREEN)    public void onSubscribeScreen(SocketIOClient client, String deviceId, AckRequest ackRequest) {        log.info("subscribe screen info, {}", deviceId);        if(StringUtils.isEmpty(deviceId)){            client.sendEvent("error", "deviceId不能为空");            return;        }        if(!subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue(), client.getSessionId().toString())){            subscribeMgr.subscribeDeviceEvent(deviceId, WSFuncEnum.SCREEN.getValue(), client.getSessionId().toString());        }        Map<String, Object> config = new HashMap<>();        config.put("scale", 0.5f);        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);        if(observer != null ){            Message message = Message.newBuilder().setTopicName(Message.Topic.COMPONENT_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(config))).build();            observer.onNext(message);            message = Message.newBuilder().setTopicName(Message.Topic.SCREEN_START).setStatus("OK").setMessage(ByteString.copyFromUtf8("screen start")).build();            observer.onNext(message);        }else{            client.sendEvent("error", "设备还未准备好");        }        Device device = deviceService.findByDeviceId(deviceId);        client.sendEvent("devices", JSON.toJSON(device));    }    //    private void requestStartMinitouch(String deviceId) throws ObjectNotExistsException {//        MiniCmd cmd = new MiniCmd();//        cmd.setType("minitouch");//        pushCmdService.pushMinCmdStart(cmd, deviceId);//    }////    private void requestStartMinicap(String deviceId) throws ObjectNotExistsException {//        Map<String, Object> config = new HashMap<>();////        config.put("rotate", 0.0f);//        config.put("scale", 0.25f);//        MiniCmd cmd = new MiniCmd();//        cmd.setType("minicap");//        cmd.setConfig(config);//        pushCmdService.pushMinCmdStart(cmd, deviceId);//    }//    private void requestStartStfService(String deviceId) throws ObjectNotExistsException {//        MiniCmd cmd = new MiniCmd();//        cmd.setType("stfagent");//        pushCmdService.pushStfAgentCmdStart(cmd, deviceId);//    }    @OnEvent(value = WAIT_SCREEN)    public void onWaitScreen(SocketIOClient client, String deviceId, AckRequest ackRequest) {        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.SCREEN.getValue(), client.getSessionId().toString())){            subscribeMgr.delSubscribe(deviceId, WSFuncEnum.SCREEN.getValue(), client.getSessionId().toString());//            pushCmdService.pushScreenUploadStop(deviceId);            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);            if(observer != null ){                Message message = Message.newBuilder().setTopicName(Message.Topic.SCREEN_WAIT).setStatus("OK").setMessage(ByteString.copyFromUtf8("screen waite ...... ")).build();                observer.onNext(message);            }else{                client.sendEvent("error", "设备还未准备好");            }        }    }    /**     * logcat cmd eg: adb logcat ActivityManager:I MyApp:D *:S     * 订阅logcat消息，需要先拼接好命令     * @param client     * @param data {"deviceId": "", "content": "ActivityManager:I MyApp:D *:S"}     * @param ackRequest     */    @OnEvent(value = SUB_LOGCAT)    public void onSubscribeLogcat(SocketIOClient client, String data, AckRequest ackRequest) {        log.info("subscribe logcat info, {}, {}", data, client.getSessionId().toString());        if(StringUtils.isEmpty(data)){            log.error("data is null");            client.sendEvent("error", "data不能为空");            return;        }        Map params = JSON.parseObject(data, Map.class);        String deviceId = (String) params.get("deviceId");        if(StringUtils.isEmpty(deviceId)){            log.error("deviceId is null");            client.sendEvent("error", "deviceId不能为空");            return;        }        String content = (String) params.get("content");        // content可以为空//        if(StringUtils.isEmpty(content)){//            client.sendEvent("error", "content不能为空");//            return;//        }        subscribeMgr.subscribeDeviceEvent(deviceId, WSFuncEnum.LOGCAT.getValue(), client.getSessionId().toString());        if(subscribeMgr.isSubscribes(deviceId, WSFuncEnum.LOGCAT.getValue(), client.getSessionId().toString())){//            pushCmdService.pushLogcatUploadStart(deviceId, content);            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);            if(observer != null ){                Message message = Message.newBuilder().setTopicName(Message.Topic.LOGCAT_START).setStatus("OK").setMessage(ByteString.copyFromUtf8("logcat start ...... ")).build();                observer.onNext(message);            }else{                client.sendEvent("error", "设备还未准备好");            }        }    }    @OnEvent(value = WAIT_LOGCAT)    public void onWaitLogcat(SocketIOClient client, String deviceId, AckRequest ackRequest) {        log.info("cancel logcat info, {}, {}", deviceId, client.getSessionId().toString());        subscribeMgr.delSubscribe(deviceId, WSFuncEnum.LOGCAT.getValue(), client.getSessionId().toString());//        pushCmdService.pushLogcatUploadStop(deviceId);        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);        if(observer != null ){            Message message = Message.newBuilder().setTopicName(Message.Topic.LOGCAT_STOP).setStatus("OK").setMessage(ByteString.copyFromUtf8("stop ...... ")).build();            observer.onNext(message);        }else{            client.sendEvent("error", "设备还未准备好");        }    }}