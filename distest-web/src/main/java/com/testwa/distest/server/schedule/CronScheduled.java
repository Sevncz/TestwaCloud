package com.testwa.distest.server.schedule;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.google.protobuf.ByteString;
import com.testwa.core.redis.RedisCacheManager;
import com.testwa.distest.server.mongo.model.ProcedureInfo;
import com.testwa.distest.server.service.cache.mgr.DeviceSessionMgr;
import com.testwa.distest.server.service.rpc.cache.CacheUtil;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.web.task.execute.ProcedureRedisMgr;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.push.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@Slf4j
@Component
public class CronScheduled {

    @Autowired
    private ProcedureRedisMgr procedureRedisMgr;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private Environment env;


    @Scheduled(cron = "0/10 * * * * ?")
    public void saveRunningLog() {
        Long logSize = procedureRedisMgr.size();
        if(logSize == null || logSize == 0){
            return;
        }

        List<ProcedureInfo> logers = new ArrayList<>();
        for(int i=0;i < logSize; i++){
            String info = procedureRedisMgr.getProcedureFromQueue();
            try {
                ProcedureInfo pi = JSON.parseObject(info, ProcedureInfo.class);
                if(!StringUtils.isBlank(pi.getDeviceId())){
                    logers.add(pi);
                }
            }catch (Exception e){
                log.error("running log transfer error", e);
//                procedureRedisMgr.addProcedureToQueue(info);
//                procedureRedisMgr.addErrorProcedureToQueue(info);
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
    @Scheduled(cron = "0/5 * * * * ?")
    public void checkDeviceOnline(){
        Set<String> deviceIds = deviceAuthMgr.allOnlineDevices();
        log.debug("online device num: {}", deviceIds.size());
        deviceIds.forEach( d -> {
            StreamObserver<Message> observer = CacheUtil.serverCache.getObserver(d);
            if(observer == null){
                log.error("{} observer is null", d);
                deviceAuthMgr.offline(d);
            }else{
                try{
                    Message message = Message.newBuilder().setTopicName(Message.Topic.ADB).setStatus("OK").setMessage(ByteString.copyFromUtf8("0")).build();
                    observer.onNext(message);
                }catch (Exception e) {
                    deviceAuthMgr.offline(d);
                    log.error("通信失败", e);
                }
            }
        });
    }
//
//    @Scheduled(cron = "0 1 * * * ?")
//    public void cleanTemp(){
//
//        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), env.getProperty("server.context-path").replace("/", ""));
//        if(Files.exists(tempPath)){
//            try {
//                Files.walkFileTree(tempPath, new FileVisitor<Path>() {
//                    @Override
//                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                        return FileVisitResult.CONTINUE;
//                    }
//
//                    @Override
//                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                        Files.delete(file);
//                        return FileVisitResult.CONTINUE;
//                    }
//
//                    @Override
//                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
//                        log.error("delete files error ", file.toString(), exc);
//                        return FileVisitResult.CONTINUE;
//                    }
//
//                    @Override
//                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//                        Files.delete(dir);
//                        return FileVisitResult.CONTINUE;
//                    }
//                });
//            } catch (IOException e) {
//                log.error("delete file error ", tempPath.toString(), e);
//            }
//        }
//
//    }

}