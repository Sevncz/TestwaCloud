package com.testwa.distest.server.websocket.handler;import com.alibaba.fastjson.JSON;import com.corundumstudio.socketio.AckRequest;import com.corundumstudio.socketio.SocketIOClient;import com.corundumstudio.socketio.annotation.OnEvent;import com.google.protobuf.ByteString;import com.testwa.core.base.exception.DeviceUnusableException;import com.testwa.core.base.exception.ObjectNotExistsException;import com.testwa.distest.common.enums.DB;import com.testwa.distest.server.entity.Device;import com.testwa.distest.server.entity.DeviceLog;import com.testwa.distest.server.service.cache.mgr.DeviceLockCache;import com.testwa.distest.server.service.cache.queue.LogQueue;import com.testwa.distest.server.service.cache.queue.ScreenProjectionQueue;import com.testwa.distest.server.service.device.service.DeviceLogService;import com.testwa.distest.server.service.device.service.DeviceService;import com.testwa.distest.server.rpc.cache.CacheUtil;import com.testwa.distest.server.web.device.mgr.DeviceLockMgr;import com.testwa.distest.server.web.device.validator.DeviceValidatoer;import io.grpc.stub.StreamObserver;import io.rpc.testwa.push.Message;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Component;import java.nio.charset.StandardCharsets;import java.util.HashMap;import java.util.Map;import java.util.concurrent.*;@Slf4j@Componentpublic class DeviceDebugHandler {    private final static String SUB_SCREEN = "sub_screen";    private final static String WAIT_SCREEN = "wait_screen";    private final static String SUB_LOGCAT = "sub_logcat";    private final static String WAIT_LOGCAT = "wait_logcat";    private final static String LOGCAT_FORMATE = "-v threadtime";    private final static String DEBUG_ERROR = "debug_error";    // 暂定同时支持100个任务并发//    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(100);    private final ExecutorService executorService = Executors.newFixedThreadPool(10);    private final ConcurrentHashMap<String, DeviceLog> futures = new ConcurrentHashMap<>();    private final ConcurrentHashMap<String, DeviceLog> logFutures = new ConcurrentHashMap<>();    @Autowired    private DeviceService deviceService;    @Autowired    private DeviceValidatoer deviceValidatoer;    @Autowired    private ScreenProjectionQueue screenStreamQueue;    @Autowired    private LogQueue logQueue;    @Autowired    private DeviceLockMgr deviceLockMgr;    @Autowired    private DeviceLogService deviceLogService;    /**     *@Description: 订阅设备屏幕     *@Param: [client, deviceId, ackRequest]     *@Return: void     *@Author: wen     *@Date: 2018/4/28     */    @OnEvent(value = SUB_SCREEN)    public void onSubscribeScreen(SocketIOClient client, String deviceId, AckRequest ackRequest) {        log.info("subscribe screen info, {}", deviceId);        if(StringUtils.isEmpty(deviceId)){            client.sendEvent(DEBUG_ERROR, "deviceId不能为空");            return;        }        try {            deviceValidatoer.validateUsable(deviceId);        }catch (DeviceUnusableException | ObjectNotExistsException e) {            log.error("设备忙碌中 {}", deviceId);            client.sendEvent(DEBUG_ERROR, "设备忙碌中");            return;        }        Map<String, Object> config = new HashMap<>();        config.put("scale", 0.5f);        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);        if(observer != null ){            // 通知设备启动            Message message = Message.newBuilder().setTopicName(Message.Topic.COMPONENT_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(config))).build();            observer.onNext(message);            message = Message.newBuilder().setTopicName(Message.Topic.SCREEN_START).setStatus("OK").setMessage(ByteString.copyFromUtf8("screen start")).build();            observer.onNext(message);            deviceLockMgr.debugLock(deviceId, "debugging......");        }else{            log.error("设备还未准备好 {}", deviceId);            client.sendEvent(DEBUG_ERROR, "设备还未准备好");            return;        }        Device device = deviceService.findByDeviceId(deviceId);        // 传输图像任务        screenStreamQueue.clear(deviceId);        final DeviceLog devLog = new DeviceLog(deviceId, DB.DeviceLogType.DEBUG);        executorService.submit(() -> {            while(devLog.isRunning() && client.isChannelOpen()) {                try {                    Object obj = screenStreamQueue.pop(deviceId);                    if(obj != null) {                        byte[] imgData = (byte[]) obj;                        if(imgData.length != 0) {                            client.sendEvent("minicap", imgData);                            continue;                        }                    }                } catch (Exception e) {                    log.warn("Sender img to ws client error, close {} ws connection", deviceId, e);                    devLog.setRunning(false);                }            }            devLog.setEndTime(System.currentTimeMillis());            log.info("ws connect status {} {} ", client.isChannelOpen(), devLog.toString());            StreamObserver<Message> devObserver = CacheUtil.serverCache.getObserver(deviceId);            if(devObserver != null ) {                Message message = Message.newBuilder().setTopicName(Message.Topic.COMPONENT_STOP).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(config))).build();                devObserver.onNext(message);                message = Message.newBuilder().setTopicName(Message.Topic.SCREEN_WAIT).setStatus("OK").setMessage(ByteString.copyFromUtf8("screen wait")).build();                devObserver.onNext(message);                futures.remove(deviceId);            }            deviceLockMgr.debugRelease(deviceId);            deviceLogService.insert(devLog);        });        futures.put(deviceId, devLog);        client.sendEvent("devices", JSON.toJSON(device));    }    /**     * logcat cmd eg: adb logcat ActivityManager:I MyApp:D *:S     * 订阅logcat消息，需要先拼接好命令     * @param client     * @param data {"deviceId": "", "content": "ActivityManager:I MyApp:D *:S"}     * @param ackRequest     */    @OnEvent(value = SUB_LOGCAT)    public void onSubscribeLogcat(SocketIOClient client, String data, AckRequest ackRequest) {        log.info("subscribe logcat info, {}, {}", data, client.getSessionId().toString());        if(StringUtils.isEmpty(data)){            client.sendEvent(DEBUG_ERROR, "data不能为空");            return;        }        Map params = JSON.parseObject(data, Map.class);        String deviceId = (String) params.get("deviceId");        if(StringUtils.isEmpty(deviceId)){            log.error("deviceId is null");            client.sendEvent(DEBUG_ERROR, "deviceId不能为空");            return;        }        String content = (String) params.get("content");        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);        if(observer != null ){            content = String.format("%s %s", LOGCAT_FORMATE, content);            Message message = Message.newBuilder().setTopicName(Message.Topic.LOGCAT_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(content)).build();            observer.onNext(message);        }else{            log.error("设备还未准备好 {}", deviceId);            client.sendEvent(DEBUG_ERROR, "设备还未准备好");            return;        }        final DeviceLog devLog = new DeviceLog(deviceId, DB.DeviceLogType.DEBUG);        executorService.submit(() -> {            while(devLog.isRunning() && client.isChannelOpen()) {                try {                    Object obj = logQueue.pop(deviceId);                    if(obj != null) {                        byte[] bytes = (byte[]) obj;                        if(bytes.length != 0) {                            String logstr = new String(bytes, StandardCharsets.UTF_8).replace("\0", "");                            // 正则解析 logstr                            client.sendEvent("logcat", logstr);                            continue;                        }                    }                } catch (Exception e) {                    log.warn("Sender img to ws client error, close {} ws connection", deviceId, e);                }            }            log.info("ws connect status {} {} ", client.isChannelOpen(), devLog.toString());            StreamObserver<Message> devObserver = CacheUtil.serverCache.getObserver(deviceId);            if(devObserver != null ) {                Message message = Message.newBuilder().setTopicName(Message.Topic.LOGCAT_STOP).setStatus("OK").setMessage(ByteString.copyFromUtf8("stop")).build();                devObserver.onNext(message);                logFutures.remove(deviceId);            }        });        logFutures.put(deviceId, devLog);    }    @OnEvent(value = WAIT_SCREEN)    public void onWaitScreen(SocketIOClient client, String deviceId, AckRequest ackRequest) {        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);        DeviceLog deviceLog = futures.get(deviceId);        if(deviceLog != null) {            deviceLog.setRunning(false);        }        futures.remove(deviceId);        if(observer != null ){        }else{            log.error("设备还未准备好 {}", deviceId);            client.sendEvent(DEBUG_ERROR, "设备还未准备好");            return;        }    }    @OnEvent(value = WAIT_LOGCAT)    public void onWaitLogcat(SocketIOClient client, String deviceId, AckRequest ackRequest) {        log.info("cancel logcat info, {}, {}", deviceId, client.getSessionId().toString());        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);        DeviceLog deviceLog = logFutures.get(deviceId);        if(deviceLog != null) {            deviceLog.setRunning(false);        }        logFutures.remove(deviceId);        if(observer != null ){            Message message = Message.newBuilder().setTopicName(Message.Topic.LOGCAT_STOP).setStatus("OK").setMessage(ByteString.copyFromUtf8("stop ...... ")).build();            observer.onNext(message);        }else{            log.error("设备还未准备好 {}", deviceId);            client.sendEvent(DEBUG_ERROR, "设备还未准备好");            return;        }    }}