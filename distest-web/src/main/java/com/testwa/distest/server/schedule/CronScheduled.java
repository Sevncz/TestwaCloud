package com.testwa.distest.server.schedule;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.mongo.model.Performance;
import com.testwa.distest.server.mongo.model.AppiumRunningLog;
import com.testwa.distest.server.rpc.cache.CacheUtil;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.web.device.mgr.DeviceOnlineMgr;
import com.testwa.distest.server.web.device.mgr.DeviceLockMgr;
import com.testwa.distest.server.web.task.mgr.PerformanceRedisMgr;
import com.testwa.distest.server.web.task.mgr.ProcedureRedisMgr;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.push.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CronScheduled {

    @Autowired
    private ProcedureRedisMgr procedureRedisMgr;
    @Autowired
    private PerformanceRedisMgr performanceRedisMgr;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private DeviceOnlineMgr deviceOnlineMgr;
    @Autowired
    private DeviceLockMgr deviceLockMgr;
    @Autowired
    private DeviceService deviceService;

    @Async
    @Scheduled(fixedDelay = 8000)
    public void saveRunningLog() {
        Long logSize = procedureRedisMgr.size();
        if(logSize == null || logSize == 0){
            return;
        }

        List<AppiumRunningLog> logers = new ArrayList<>();
        for(int i=0;i < logSize; i++){
            String info = procedureRedisMgr.getProcedureFromQueue();
            try {
                AppiumRunningLog pi = JSON.parseObject(info, AppiumRunningLog.class);
                if(!StringUtils.isBlank(pi.getDeviceId())){
                    logers.add(pi);
                }
            }catch (Exception e){
                log.error("running log transfer error", e);
            }
        }
        mongoTemplate.insertAll(logers);

    }

    @Async
    @Scheduled(fixedDelay = 8000)
    public void savePerformance() {
        Long logSize = performanceRedisMgr.size();
        if(logSize == null || logSize == 0){
            return;
        }

        List<Performance> logers = new ArrayList<>();
        for(int i=0;i < logSize; i++){
            String p = performanceRedisMgr.getPerformanceFormQueue();
            try {
                Performance entity = JSON.parseObject(p, Performance.class);
                if(!StringUtils.isBlank(entity.getDeviceId())){
                    logers.add(entity);
                }
            }catch (Exception e){
                log.error("Performance log transfer error {}", p, e);
            }
        }
        mongoTemplate.insertAll(logers);
    }

    /**
     *@Description: 清理不正常的observer，（这里还是有一定误删除的可能）
     *@Param: []
     *@Return: void
     *@Author: wen
     *@Date: 2018/5/9
     */
    @Async
    @Scheduled(fixedDelay = 5000)
    public void checkDeviceOnline(){
        Set<String> deviceIds = deviceOnlineMgr.allOnlineDevices();
        log.debug("online device num: {}", deviceIds.size());
        deviceIds.forEach( d -> {
            log.debug("Check StreamObserver deviceId {}", d);
            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(d);
            if(observer == null){
                log.error("{} observer is null", d);
                deviceOnlineMgr.offline(d, DB.PhoneOnlineStatus.DISCONNECT);
            }else{
                int tryTime = 5;
                while(tryTime >= 0) {
                    try{
                        Message message = Message.newBuilder().setTopicName(Message.Topic.ADB).setMessage(ByteString.copyFromUtf8("0")).build();
                        observer.onNext(message);
                        break;
                    }catch (Exception e) {
                        tryTime--;
                        try {
                            TimeUnit.MILLISECONDS.sleep(200);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                if(tryTime <= 0) {
                    deviceOnlineMgr.offline(d, DB.PhoneOnlineStatus.DISCONNECT);
                    log.warn("通信失败 {}", d);
                }
            }
        });
    }

    /**
     *@Description: 清理过期的设备锁
     *@Param: []
     *@Return: void
     *@Author: wen
     *@Date: 2018/5/9
     */
    @Async
    @Scheduled(fixedDelay = 10000)
    public void cleanDeviceLock(){
        List<String> lockDeviceIds = deviceLockMgr.getLockList();
        List<Device> workDevices = deviceService.findAllInWrok();
        if(workDevices != null && !workDevices.isEmpty()) {
            workDevices.forEach( d -> {
                if(!lockDeviceIds.contains(d.getDeviceId())) {
                    log.info("Device [{}-{}-{}] not locked", d.getDeviceId(), d.getBrand(), d.getModel());
                    if(!DB.DeviceWorkStatus.FREE.equals(d.getWorkStatus())){
                        deviceLockMgr.workRelease(d.getDeviceId());
                    }
                    if(!DB.DeviceDebugStatus.FREE.equals(d.getDebugStatus())){
                        deviceLockMgr.debugReleaseForce(d.getDeviceId());
                    }
                }
            });
        }
    }

}