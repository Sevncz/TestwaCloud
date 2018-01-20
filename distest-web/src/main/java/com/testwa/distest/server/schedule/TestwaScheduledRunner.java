package com.testwa.distest.server.schedule;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.redis.RedisCacheManager;
import com.testwa.distest.server.mvc.model.ProcedureInfo;
import com.testwa.distest.server.service.cache.mgr.DeviceSessionMgr;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.web.task.execute.ProcedureRedisMgr;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@Component
public class TestwaScheduledRunner {
    private ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private RedisCacheManager redisCacheManager;

    @Autowired
    private ProcedureRedisMgr procedureRedisMgr;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private Environment env;
    @Autowired
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private DeviceSessionMgr deviceSessionMgr;

    private final SocketIOServer server;

    @Autowired
    public TestwaScheduledRunner(SocketIOServer server) {
        this.server = server;
    }


    @Scheduled(cron = "0/10 * * * * ?")
    public void storeRunningLog() throws Exception {
        Long logSize = procedureRedisMgr.size();
        if(logSize == null || logSize == 0){
            return;
        }

        List<ProcedureInfo> logers = new ArrayList<>();
        for(int i=0;i < logSize; i++){
            String info = procedureRedisMgr.getProcedureFromQueue();
            try {
                ProcedureInfo pi = JSON.parseObject(info, ProcedureInfo.class);
                logers.add(pi);
            }catch (Exception e){
                log.error("running log transfer error", e);
//                procedureRedisMgr.addProcedureToQueue(info);
                procedureRedisMgr.addErrorProcedureToQueue(info);
            }
        }
        mongoTemplate.insertAll(logers);

    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void checkDeviceOnline(){
        Set<String> onlineDevices = deviceAuthMgr.allOnlineDevices();
        onlineDevices.forEach( d -> {
            String sessionId = deviceSessionMgr.getDeviceSession(d);
            if(StringUtils.isNotEmpty(sessionId)){
                SocketIOClient client = server.getClient(UUID.fromString(sessionId));
                if(client == null){
                    deviceAuthMgr.offline(d);
                }
            }
        });
        deviceAuthMgr.mergeOnline();
    }

    @Scheduled(cron = "0 1 * * * ?")
    public void cleanTemp(){

        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), env.getProperty("server.context-path").replace("/", ""));
        if(Files.exists(tempPath)){
            try {
                Files.walkFileTree(tempPath, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        log.error("delete files error ", file.toString(), exc);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                log.error("delete file error ", tempPath.toString(), e);
            }
        }

    }

}