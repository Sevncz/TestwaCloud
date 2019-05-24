package com.testwa.distest.server.websocket.handler;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.protobuf.ByteString;
import com.testwa.core.base.util.CronDateUtils;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.config.security.JwtTokenUtil;
import com.testwa.distest.exception.BusinessException;
import com.testwa.distest.exception.DeviceException;
import com.testwa.distest.quartz.job.DebugJobDataMap;
import com.testwa.distest.quartz.service.JobService;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.DeviceLog;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.cache.mgr.DeviceLoginMgr;
import com.testwa.distest.server.service.cache.mgr.SubscribeDeviceFuncMgr;
import com.testwa.distest.server.service.cache.mgr.WebsocketLoginMgr;
import com.testwa.distest.server.service.cache.queue.ScreenProjectionQueue;
import com.testwa.distest.server.service.device.service.DeviceLogService;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.rpc.cache.CacheUtil;
import com.testwa.distest.server.service.user.service.AgentLoginLoggerService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.mgr.DeviceLockMgr;
import com.testwa.distest.server.web.device.mgr.DeviceOnlineMgr;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.push.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wen on 2016/9/24.
 */
@Slf4j
@Component
public class RemoteScreenEventHandler {

    private final static String TOUCH = "touch";
    private final static String INPUT = "input";
    private final static String HOME = "home";
    private final static String BACK = "back";
    private final static String MENU = "menu";
    private final static String DEL = "del";
    private final static String CLEAR = "clear";
    private final static String GET_DEVICES = "get_devices";
    private final static String SHELL = "shell";
    private final static String WEB = "web";
    private final static String REMOTE_DEBUG_START = "remote_debug_start";
    private final static String REMOTE_DEBUG_STOP = "remote_debug_stop";

    private final static String SUB_SCREEN = "sub_screen";
    private final static String WAIT_SCREEN = "wait_screen";
    private final static String SUB_LOGCAT = "sub_logcat";
    private final static String FILTER_LOGCAT = "filter_logcat";
    private final static String WAIT_LOGCAT = "wait_logcat";
    private final static String DEBUG_ERROR = "debug_error";

    private final static String JOB_DEBUG_NAME = "com.testwa.distest.quartz.job.EquipmentDebugJob";
    private final static String JOB_LOGCAT_NAME = "com.testwa.distest.quartz.job.EquipmentLogcatJob";

    private final static String OPEN_ACTION = "open";
    private final static String CLOSE_ACTION = "close";
    private final static String STATUS_OK = "OK";

    @Autowired
    private DeviceLoginMgr deviceSessionMgr;
    @Autowired
    private SubscribeDeviceFuncMgr subscribeMgr;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private WebsocketLoginMgr clientSessionMgr;
    @Autowired
    private DeviceOnlineMgr deviceOnlineMgr;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private AgentLoginLoggerService agentLoginLoggerService;
    @Autowired
    private DeviceValidatoer deviceValidatoer;
    @Autowired
    private ScreenProjectionQueue screenStreamQueue;
    @Autowired
    private DeviceLockMgr deviceLockMgr;
    @Autowired
    private DeviceLogService deviceLogService;
    @Autowired
    private JobService jobService;

    /*------------------连接状态------------------*/

    @OnConnect
    public void onConnect(SocketIOClient client) {

        String type = client.getHandshakeData().getSingleUrlParam("type");
        if("device".equals(type)){

        }else if("client".equals(type)){
            // 客户端连接
            try {
                String token = client.getHandshakeData().getSingleUrlParam("token");
                if(StringUtils.isNotBlank(token)){
                    String username = jwtTokenUtil.getUsernameFromToken(token);
                    User user = userService.findByUsername(username);
                    clientSessionMgr.login(user.getId(), client.getSessionId().toString());
                }
            } catch (Exception e) {
                log.error("parser token error", e);
            }
        }else if("browser".equals(type)){
            // 浏览器连接, 订阅一个设备的图像输出流
        } else {
            log.error("Illegal connection");
        }
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {

        String type = client.getHandshakeData().getSingleUrlParam("type");
        if("device".equals(type)){
            // 设备连接断开
            String serial = client.getHandshakeData().getSingleUrlParam("serial");
            deviceSessionMgr.logout(serial);
            deviceOnlineMgr.offline(serial, DB.PhoneOnlineStatus.DISCONNECT);

        }else if("client".equals(type)){
            // 客户端连接断开
            // 清理该客户端的缓存
//            clientSessionMgr.delMainInfo(client.getSessionId().toString());
            try {
                String token = client.getHandshakeData().getSingleUrlParam("token");
                if(StringUtils.isNotBlank(token)){
                    String username = jwtTokenUtil.getUsernameFromToken(token);
                    User user = userService.findByUsername(username);
                    clientSessionMgr.logout(user.getId());
                    agentLoginLoggerService.updateRecentLogoutTime(user.getUsername());
                }
            } catch (Exception e) {
                log.error("parser token error", e);
            }
        }else if("browser".equals(type)){
            // 浏览器连接断开
            log.debug("browser disconnect");
            // 清理资源

        }
    }

    /*------------------屏幕和日志显示------------------*/
    /**
     *@Description: 订阅设备屏幕
     *@Param: [client, deviceId, ackRequest]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/28
     */
    @OnEvent(value = SUB_SCREEN)
    public void onSubscribeScreen(SocketIOClient client, String deviceId, AckRequest ackRequest) {
        log.info("subscribe screen info, {}", deviceId);
        if(StringUtils.isEmpty(deviceId)){
            client.sendEvent(DEBUG_ERROR, "deviceId不能为空");
            return;
        }
        try {
            deviceValidatoer.validateUsable(deviceId);
        }catch (DeviceException e) {
            log.error("设备忙碌中 {}", deviceId, e);
            client.sendEvent(DEBUG_ERROR, "设备忙碌中");
            return;
        }

        Map<String, Object> config = new HashMap<>();
        config.put("scale", 0.5f);

        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            // 通知设备启动
//            Message message = Message.newBuilder().setTopicName(Message.Topic.COMPONENT_START).setStatus(STATUS_OK).setMessage(ByteString.copyFromUtf8(JSON.toJSONString(config))).build();
//            observer.onNext(message);
            Message message = Message.newBuilder().setTopicName(Message.Topic.SCREEN_START).setStatus(STATUS_OK).setMessage(ByteString.copyFromUtf8("screen start")).build();
            observer.onNext(message);
            deviceLockMgr.debugLock(deviceId, client.getSessionId().toString());
        }else{
            log.error("设备还未准备好 {}", deviceId);
            client.sendEvent(DEBUG_ERROR, "设备还未准备好");
            return;
        }
        Device device = deviceService.findByDeviceId(deviceId);

        // 传输图像任务
        screenStreamQueue.clear(deviceId);

        // 获取token
        String userCode = getUserCode(client);

        // 保存devLog
        final DeviceLog devLog = new DeviceLog();
        devLog.setDeviceId(deviceId);
        devLog.setUserCode(userCode);
        devLog.setStartTime(System.currentTimeMillis());
        devLog.setRunning(true);
        devLog.setLogType(DB.DeviceLogType.DEBUG);

        Long devLogId = deviceLogService.insert(devLog);

        // 执行任务所需要的参数
        DebugJobDataMap params = new DebugJobDataMap();
        params.setDeviceId(deviceId);
        params.setDevLogId(devLogId);
        params.setSocketClientId(client.getSessionId().toString());

        DateTime now = new DateTime();
        String cron = CronDateUtils.getCron(now.plusSeconds(2).toDate());
        try {
            jobService.addJob(JOB_DEBUG_NAME, deviceId, cron,  String.format("设备[%s]-[%s]-[%s]远程调试",device.getBrand(),device.getModel(),device.getDeviceId()), JSON.toJSONString(params));
        } catch (BusinessException e) {
            e.printStackTrace();
        }
        client.sendEvent("devices", JSON.toJSON(device));

    }


    /**
     * logcat cmd eg: adb logcat ActivityManager:I MyApp:D *:S
     * 订阅logcat消息，需要先拼接好命令
     * @param client
     * @param data {"deviceId": "", "content": "ActivityManager:I MyApp:D *:S"}
     * @param ackRequest
     */
    @OnEvent(value = SUB_LOGCAT)
    public void onSubscribeLogcat(SocketIOClient client, String data, AckRequest ackRequest) {
        log.info("subscribe logcat info, {}, {}", data, client.getSessionId().toString());
        if(StringUtils.isEmpty(data)){
            client.sendEvent(DEBUG_ERROR, "data不能为空");
            return;
        }
        Map params = JSON.parseObject(data, Map.class);
        String deviceId = (String) params.get("deviceId");
        if(StringUtils.isEmpty(deviceId)){
            log.error("deviceId is null");
            client.sendEvent(DEBUG_ERROR, "deviceId不能为空");
            return;
        }
        Map content = (Map) params.get("filter");
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            Message message = Message.newBuilder().setTopicName(Message.Topic.LOGCAT_START).setStatus(STATUS_OK).setMessage(ByteString.copyFromUtf8(JSON.toJSONString(content))).build();
            observer.onNext(message);
        }else{
            log.error("设备还未准备好 {}", deviceId);
            client.sendEvent(DEBUG_ERROR, "设备还未准备好");
            return;
        }
        Device device = deviceService.findByDeviceId(deviceId);
        // 获取token
        String userCode = getUserCode(client);

        final DeviceLog devLog = new DeviceLog();
        devLog.setDeviceId(deviceId);
        devLog.setUserCode(userCode);
        devLog.setStartTime(System.currentTimeMillis());
        devLog.setRunning(true);
        devLog.setLogType(DB.DeviceLogType.LOGCAT);

        Long devLogId = deviceLogService.insert(devLog);

        DebugJobDataMap debugParams = new DebugJobDataMap();
        debugParams.setDeviceId(deviceId);
        debugParams.setDevLogId(devLogId);
        debugParams.setSocketClientId(client.getSessionId().toString());

        DateTime now = new DateTime();
        String cron = CronDateUtils.getCron(now.plusSeconds(2).toDate());
        try {
            jobService.addJob(JOB_LOGCAT_NAME, deviceId, cron,  String.format("设备[%s]-[%s]-[%s]获取Logcat",device.getBrand(),device.getModel(),device.getDeviceId()), JSON.toJSONString(debugParams));
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

    @OnEvent(value = WAIT_SCREEN)
    public void onWaitScreen(SocketIOClient client, String deviceId, AckRequest ackRequest) {
        try {
            jobService.interrupt(JOB_DEBUG_NAME, deviceId);
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

    @OnEvent(value = WAIT_LOGCAT)
    public void onWaitLogcat(SocketIOClient client, String deviceId, AckRequest ackRequest) {
        try {
            jobService.interrupt(JOB_LOGCAT_NAME, deviceId);
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

    protected String getUserCode(SocketIOClient client) {
        String token = client.getHandshakeData().getSingleUrlParam("token");
        return jwtTokenUtil.getUserCodeFromToken(token);
    }

    /*------------------控制事件------------------*/

    @OnEvent(value = TOUCH)
    public void onTouch(SocketIOClient client, String data, AckRequest ackRequest) {

        Map params = JSON.parseObject(data, Map.class);
        String deviceId = (String) params.get("deviceId");
        if (isIllegalDeviceId(client, deviceId)) {
            return;
        }
        String touch = (String) params.get("touch");
        if(StringUtils.isBlank(touch)){
            client.sendEvent("error", "坐标不能为空");
            return;
        }
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            Message message = Message.newBuilder().setTopicName(Message.Topic.TOUCH).setStatus(STATUS_OK).setMessage(ByteString.copyFromUtf8(touch)).build();
            observer.onNext(message);
        }else{
            client.sendEvent("error", "设备还未准备好");
        }
    }

    @OnEvent(value = INPUT)
    public void onInput(SocketIOClient client, String data, AckRequest ackRequest) {
        Map params = JSON.parseObject(data, Map.class);
        String deviceId = (String) params.get("deviceId");
        if (isIllegalDeviceId(client, deviceId)) {
            return;
        }

        String input = (String) params.get("input");
        if(StringUtils.isBlank(input)){
            client.sendEvent("error", "请输入内容");
            return;
        }
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            Message message = Message.newBuilder().setTopicName(Message.Topic.INPUT).setStatus(STATUS_OK).setMessage(ByteString.copyFromUtf8(input)).build();
            observer.onNext(message);
        }else{
            client.sendEvent("error", "设备还未准备好");
        }

    }

    @OnEvent(value = HOME)
    public void onHome(SocketIOClient client, String deviceId, AckRequest ackRequest) {
        if (isIllegalDeviceId(client, deviceId)) {
            return;
        }
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            Message message = Message.newBuilder().setTopicName(Message.Topic.HOME).setStatus(STATUS_OK).setMessage(ByteString.copyFromUtf8(HOME)).build();
            observer.onNext(message);
        }else{
            client.sendEvent("error", "设备还未准备好");
        }

    }
    @OnEvent(value = BACK)
    public void onBack(SocketIOClient client, String deviceId, AckRequest ackRequest) {
        if (isIllegalDeviceId(client, deviceId)) {
            return;
        }

        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            Message message = Message.newBuilder().setTopicName(Message.Topic.BACK).setStatus(STATUS_OK).setMessage(ByteString.copyFromUtf8(BACK)).build();
            observer.onNext(message);
        }else{
            client.sendEvent("error", "设备还未准备好");
        }

    }

    @OnEvent(value = MENU)
    public void onMenu(SocketIOClient client, String deviceId, AckRequest ackRequest) {
        if (isIllegalDeviceId(client, deviceId)) {
            return;
        }
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            Message message = Message.newBuilder().setTopicName(Message.Topic.MENU).setStatus(STATUS_OK).setMessage(ByteString.copyFromUtf8(MENU)).build();
            observer.onNext(message);
        }else{
            client.sendEvent("error", "设备还未准备好");
        }
    }

    @OnEvent(value = DEL)
    public void onDel(SocketIOClient client, String deviceId, AckRequest ackRequest) {
        if (isIllegalDeviceId(client, deviceId)) {
            return;
        }
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            Message message = Message.newBuilder().setTopicName(Message.Topic.DEL).setStatus(STATUS_OK).setMessage(ByteString.copyFromUtf8(DEL)).build();
            observer.onNext(message);
        }else{
            client.sendEvent("error", "设备还未准备好");
        }
    }

    @OnEvent(value = GET_DEVICES)
    public void onGetDevices(SocketIOClient client, String deviceId, AckRequest ackRequest) {
        if (isIllegalDeviceId(client, deviceId)) {
            return;
        }
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

    @OnEvent(value = SHELL)
    public void onShell(SocketIOClient client, String data, AckRequest ackRequest) {
        Map params = JSON.parseObject(data, Map.class);
        String deviceId = (String) params.get("deviceId");
        if (isIllegalDeviceId(client, deviceId)) {
            return;
        }

        String cmd = (String) params.get("cmd");
        if(StringUtils.isBlank(cmd)){
            client.sendEvent("error", "shell命令不能为空");
            return;
        }
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            Message message = Message.newBuilder().setTopicName(Message.Topic.SHELL).setStatus(STATUS_OK).setMessage(ByteString.copyFromUtf8(data)).build();
            observer.onNext(message);
        }else{
            client.sendEvent("error", "设备还未准备好");
        }
    }

    @OnEvent(value = WEB)
    public void onWeb(SocketIOClient client, String data, AckRequest ackRequest) {
        Map params = JSON.parseObject(data, Map.class);
        String deviceId = (String) params.get("deviceId");
        if (isIllegalDeviceId(client, deviceId)) {
            return;
        }

        String url = (String) params.get("url");
        if(StringUtils.isBlank(url)){
            client.sendEvent("error", "网址不能为空");
            return;
        }
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            Message message = Message.newBuilder().setTopicName(Message.Topic.OPENWEB).setStatus(STATUS_OK).setMessage(ByteString.copyFromUtf8(url)).build();
            observer.onNext(message);
        }else{
            client.sendEvent("error", "设备还未准备好");
        }
    }

    /*------------------DEBUG状态------------------*/

    @OnEvent(value = REMOTE_DEBUG_START)
    public void onRemoteDebugStart(SocketIOClient client, String deviceId, AckRequest ackRequest) {
        if (isIllegalDeviceId(client, deviceId)) {
            return;
        }
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            // 通知agent，启动远程debug
            Message message = Message.newBuilder().setTopicName(Message.Topic.DEBUG_START).setStatus(STATUS_OK).setMessage(ByteString.copyFromUtf8(REMOTE_DEBUG_START)).build();
            observer.onNext(message);
            client.sendEvent(REMOTE_DEBUG_START, "已启动");
        }else{
            client.sendEvent("error", "设备还未准备好");
        }
    }

    @OnEvent(value = REMOTE_DEBUG_STOP)
    public void onRemoteDebugStop(SocketIOClient client, String deviceId, AckRequest ackRequest) {
        if (isIllegalDeviceId(client, deviceId)) {
            return;
        }
        StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(deviceId);
        if(observer != null ){
            Message message = Message.newBuilder().setTopicName(Message.Topic.DEBUG_STOP).setStatus(STATUS_OK).setMessage(ByteString.copyFromUtf8(REMOTE_DEBUG_STOP)).build();
            observer.onNext(message);
            client.sendEvent(REMOTE_DEBUG_STOP, "已停止");
        }else{
            client.sendEvent("error", "设备还未准备好");
        }
    }

}
