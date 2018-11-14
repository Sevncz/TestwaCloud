package com.testwa.distest.server.websocket.handler;import com.alibaba.fastjson.JSON;import com.corundumstudio.socketio.AckRequest;import com.corundumstudio.socketio.SocketIOClient;import com.corundumstudio.socketio.annotation.OnEvent;import com.google.protobuf.ByteString;import com.testwa.core.base.util.CronDateUtils;import com.testwa.distest.common.enums.DB;import com.testwa.distest.config.security.JwtTokenUtil;import com.testwa.distest.exception.BusinessException;import com.testwa.distest.exception.DeviceException;import com.testwa.distest.quartz.job.DebugJobDataMap;import com.testwa.distest.quartz.service.JobService;import com.testwa.distest.server.entity.Device;import com.testwa.distest.server.entity.DeviceLog;import com.testwa.distest.server.service.cache.queue.ScreenProjectionQueue;import com.testwa.distest.server.service.device.service.DeviceLogService;import com.testwa.distest.server.service.device.service.DeviceService;import com.testwa.distest.server.rpc.cache.CacheUtil;import com.testwa.distest.server.web.device.mgr.DeviceLockMgr;import com.testwa.distest.server.web.device.validator.DeviceValidatoer;import io.grpc.stub.StreamObserver;import io.rpc.testwa.push.Message;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import org.joda.time.DateTime;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Component;import java.util.HashMap;import java.util.Map;@Slf4j@Componentpublic class DeviceDebugHandler {    private final static String SUB_SCREEN = "sub_screen";    private final static String WAIT_SCREEN = "wait_screen";    private final static String SUB_LOGCAT = "sub_logcat";    private final static String FILTER_LOGCAT = "filter_logcat";    private final static String WAIT_LOGCAT = "wait_logcat";    private final static String DEBUG_ERROR = "debug_error";    private final static String JOB_DEBUG_NAME = "com.testwa.distest.quartz.job.EquipmentDebugJob";    private final static String JOB_LOGCAT_NAME = "com.testwa.distest.quartz.job.EquipmentLogcatJob";    @Autowired    private DeviceService deviceService;    @Autowired    private DeviceValidatoer deviceValidatoer;    @Autowired    private ScreenProjectionQueue screenStreamQueue;    @Autowired    private DeviceLockMgr deviceLockMgr;    @Autowired    private DeviceLogService deviceLogService;    @Autowired    private JobService jobService;    @Autowired    private JwtTokenUtil jwtTokenUtil;    /**     *@Description: 订阅设备屏幕     *@Param: [client, deviceId, ackRequest]     *@Return: void     *@Author: wen     *@Date: 2018/4/28     */    @OnEvent(value = SUB_SCREEN)    public void onSubscribeScreen(SocketIOClient client, String deviceId, AckRequest ackRequest) {        log.info("subscribe screen info, {}", deviceId);        if(StringUtils.isEmpty(deviceId)){            client.sendEvent(DEBUG_ERROR, "deviceId不能为空");            return;        }        try {            deviceValidatoer.validateUsable(deviceId);        }catch (DeviceException e) {            log.error("设备忙碌中 {}", deviceId, e);            client.sendEvent(DEBUG_ERROR, "设备忙碌中");            return;        }        Map<String, Object> config = new HashMap<>();        config.put("scale", 0.5f);        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);        if(observer != null ){            // 通知设备启动            Message message = Message.newBuilder().setTopicName(Message.Topic.COMPONENT_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(config))).build();            observer.onNext(message);            message = Message.newBuilder().setTopicName(Message.Topic.SCREEN_START).setStatus("OK").setMessage(ByteString.copyFromUtf8("screen start")).build();            observer.onNext(message);            deviceLockMgr.debugLock(deviceId, client.getSessionId().toString());        }else{            log.error("设备还未准备好 {}", deviceId);            client.sendEvent(DEBUG_ERROR, "设备还未准备好");            return;        }        Device device = deviceService.findByDeviceId(deviceId);        // 传输图像任务        screenStreamQueue.clear(deviceId);        // 获取token        String userCode = getUserCode(client);        // 保存devLog        final DeviceLog devLog = new DeviceLog();        devLog.setDeviceId(deviceId);        devLog.setUserCode(userCode);        devLog.setStartTime(System.currentTimeMillis());        devLog.setRunning(true);        devLog.setLogType(DB.DeviceLogType.DEBUG);        Long devLogId = deviceLogService.insert(devLog);        // 执行任务所需要的参数        DebugJobDataMap params = new DebugJobDataMap();        params.setDeviceId(deviceId);        params.setDevLogId(devLogId);        params.setSocketClientId(client.getSessionId().toString());        DateTime now = new DateTime();        String cron = CronDateUtils.getCron(now.plusSeconds(2).toDate());        try {            jobService.addJob(JOB_DEBUG_NAME, deviceId, cron,  String.format("设备[%s]-[%s]-[%s]远程调试",device.getBrand(),device.getModel(),device.getDeviceId()), JSON.toJSONString(params));        } catch (BusinessException e) {            e.printStackTrace();        }        client.sendEvent("devices", JSON.toJSON(device));    }    /**     * logcat cmd eg: adb logcat ActivityManager:I MyApp:D *:S     * 订阅logcat消息，需要先拼接好命令     * @param client     * @param data {"deviceId": "", "content": "ActivityManager:I MyApp:D *:S"}     * @param ackRequest     */    @OnEvent(value = SUB_LOGCAT)    public void onSubscribeLogcat(SocketIOClient client, String data, AckRequest ackRequest) {        log.info("subscribe logcat info, {}, {}", data, client.getSessionId().toString());        if(StringUtils.isEmpty(data)){            client.sendEvent(DEBUG_ERROR, "data不能为空");            return;        }        Map params = JSON.parseObject(data, Map.class);        String deviceId = (String) params.get("deviceId");        if(StringUtils.isEmpty(deviceId)){            log.error("deviceId is null");            client.sendEvent(DEBUG_ERROR, "deviceId不能为空");            return;        }        Map content = (Map) params.get("filter");        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);        if(observer != null ){//            content = String.format("%s %s", LOGCAT_FORMATE, content);            Message message = Message.newBuilder().setTopicName(Message.Topic.LOGCAT_START).setStatus("OK").setMessage(ByteString.copyFromUtf8(JSON.toJSONString(content))).build();            observer.onNext(message);        }else{            log.error("设备还未准备好 {}", deviceId);            client.sendEvent(DEBUG_ERROR, "设备还未准备好");            return;        }        Device device = deviceService.findByDeviceId(deviceId);        // 获取token        String userCode = getUserCode(client);        final DeviceLog devLog = new DeviceLog();        devLog.setDeviceId(deviceId);        devLog.setUserCode(userCode);        devLog.setStartTime(System.currentTimeMillis());        devLog.setRunning(true);        devLog.setLogType(DB.DeviceLogType.LOGCAT);        Long devLogId = deviceLogService.insert(devLog);        DebugJobDataMap debugParams = new DebugJobDataMap();        debugParams.setDeviceId(deviceId);        debugParams.setDevLogId(devLogId);        debugParams.setSocketClientId(client.getSessionId().toString());        DateTime now = new DateTime();        String cron = CronDateUtils.getCron(now.plusSeconds(2).toDate());        try {            jobService.addJob(JOB_LOGCAT_NAME, deviceId, cron,  String.format("设备[%s]-[%s]-[%s]获取Logcat",device.getBrand(),device.getModel(),device.getDeviceId()), JSON.toJSONString(debugParams));        } catch (BusinessException e) {            e.printStackTrace();        }    }    @OnEvent(value = WAIT_SCREEN)    public void onWaitScreen(SocketIOClient client, String deviceId, AckRequest ackRequest) {        try {            jobService.interrupt(JOB_DEBUG_NAME, deviceId);        } catch (BusinessException e) {            e.printStackTrace();        }    }    @OnEvent(value = WAIT_LOGCAT)    public void onWaitLogcat(SocketIOClient client, String deviceId, AckRequest ackRequest) {        try {            jobService.interrupt(JOB_LOGCAT_NAME, deviceId);        } catch (BusinessException e) {            e.printStackTrace();        }    }    protected String getUserCode(SocketIOClient client) {        String token = client.getHandshakeData().getSingleUrlParam("token");        return jwtTokenUtil.getUserCodeFromToken(token);    }}