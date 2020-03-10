package com.testwa.distest.quartz.job;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.google.protobuf.ByteString;
import com.testwa.distest.server.entity.DeviceLog;
import com.testwa.distest.server.rpc.cache.CacheUtil;
import com.testwa.distest.server.service.cache.queue.ScreenProjectionQueue;
import com.testwa.distest.server.service.device.service.DeviceLogService;
import com.testwa.distest.server.web.device.mgr.DeviceLockMgr;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.agent.Message;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@DisallowConcurrentExecution
public class EquipmentDebugJob implements BaseJob, InterruptableJob {
    private boolean _interrupted = false;

    @Autowired
    private DeviceLogService deviceLogService;
    @Autowired
    private SocketIOServer server;
    @Autowired
    private ScreenProjectionQueue screenStreamQueue;
    @Autowired
    private DeviceLockMgr deviceLockMgr;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String paramsStr = dataMap.getString("params");
        DebugJobDataMap params = JSON.parseObject(paramsStr, DebugJobDataMap.class);

        String socketClientId = params.getSocketClientId();
        String deviceId = params.getDeviceId();
        Long devLogId = params.getDevLogId();

        DeviceLog devLog = deviceLogService.get(devLogId);
        SocketIOClient client = server.getClient(UUID.fromString(socketClientId));
        if(client == null) {
            deviceLockMgr.debugRelease(deviceId, socketClientId);
            if(devLog == null) {
                return;
            }
            devLog.setEndTime(System.currentTimeMillis());
            try {
                deviceLogService.update(devLog);
            }catch (Exception e) {
                log.error("[EquipmentDebugJob] 更新devLog失败, {}", JSON.toJSONString(devLog), e);
            }
            return;
        }

        while(client.isChannelOpen() && !_interrupted) {
            try {
                Object obj = screenStreamQueue.pop(deviceId);
                if(obj != null) {
                    byte[] imgData = (byte[]) obj;
                    log.debug("[{}] 获取屏幕，bytes 长度 {}", deviceId, imgData.length);
                    if(imgData.length != 0) {
                        log.debug("[{}] client {} channel {}", deviceId, client.isChannelOpen());
                        client.sendEvent("minicap", imgData);
                        continue;
                    }
                }
            } catch (Exception e) {
                log.warn("Sender img to ws client error, close {} ws connection", deviceId, e);
            }

        }
        log.info("ws connect status {} {} ", client.isChannelOpen(), devLog.toString());
        StreamObserver<Message> devObserver = CacheUtil.serverCache.getObserver(deviceId);
        if(devObserver != null ) {
            Message message = Message.newBuilder().setTopicName(Message.Topic.PROJECTION_STOP).setStatus("OK").setMessage(ByteString.copyFromUtf8("screen wait")).build();
            devObserver.onNext(message);
        }
        deviceLockMgr.debugRelease(deviceId, socketClientId);

        devLog.setRunning(false);
        devLog.setEndTime(System.currentTimeMillis());
        deviceLogService.update(devLog);

    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        _interrupted = true;
    }
}