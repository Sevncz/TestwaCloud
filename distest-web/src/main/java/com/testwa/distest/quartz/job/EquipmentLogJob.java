package com.testwa.distest.quartz.job;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.google.protobuf.ByteString;
import com.testwa.distest.server.entity.DeviceLog;
import com.testwa.distest.server.rpc.cache.CacheUtil;
import com.testwa.distest.server.service.cache.queue.LogQueue;
import com.testwa.distest.server.service.device.service.DeviceLogService;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.push.Message;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@DisallowConcurrentExecution
public class EquipmentLogJob implements BaseJob, InterruptableJob {
    private boolean _interrupted = false;

    @Autowired
    private DeviceLogService deviceLogService;
    @Autowired
    private SocketIOServer server;
    @Autowired
    private LogQueue logQueue;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("EquipmentLogJob: 启动日志收集任务");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String paramsStr = dataMap.getString("params");
        DebugJobDataMap params = JSON.parseObject(paramsStr, DebugJobDataMap.class);

        String socketClientId = params.getSocketClientId();
        String deviceId = params.getDeviceId();
        Long devLogId = params.getDevLogId();

        DeviceLog devLog = deviceLogService.get(devLogId);
        SocketIOClient client = server.getClient(UUID.fromString(socketClientId));

        if(client == null) {
            if(devLog == null) {
                return;
            }
            devLog.setRunning(false);
            devLog.setEndTime(System.currentTimeMillis());
            deviceLogService.update(devLog);
            return;
        }

        while(!_interrupted && client.isChannelOpen()) {
            try {
                Object obj = logQueue.pop(deviceId);
                if(obj != null) {
                    byte[] bytes = (byte[]) obj;
                    log.debug("获取{}日志，bytes 长度 {}", deviceId, bytes.length);
                    if(bytes.length != 0) {
                        client.sendEvent("logcat", new String(bytes));
                        continue;
                    }
                }
            } catch (Exception e) {
                log.warn("EquipmentLogJob: Sender img to ws client error, close {} ws connection", deviceId, e);
            }
        }
        log.info("EquipmentLogJob: ws connect status {} {} ", client.isChannelOpen(), devLog.toString());
        StreamObserver<Message> devObserver = CacheUtil.serverCache.getObserver(deviceId);
        if(devObserver != null ) {
            Message message = Message.newBuilder().setTopicName(Message.Topic.LOGCAT_STOP).setStatus("OK").setMessage(ByteString.copyFromUtf8("stop")).build();
            devObserver.onNext(message);
        }
        devLog.setEndTime(System.currentTimeMillis());
        devLog.setRunning(false);
        deviceLogService.update(devLog);
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        _interrupted = true;
    }
}