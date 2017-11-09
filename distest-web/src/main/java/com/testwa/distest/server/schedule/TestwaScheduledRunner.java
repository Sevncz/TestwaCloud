package com.testwa.distest.server.schedule;

import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.redis.RedisCacheManager;
import com.testwa.distest.server.mvc.model.ProcedureInfo;
import com.testwa.distest.server.service.cache.mgr.DeviceCacheMgr;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class TestwaScheduledRunner {
    private static final Logger log = LoggerFactory.getLogger(TestwaScheduledRunner.class);
    private ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private RedisCacheManager redisCacheManager;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private DeviceCacheMgr deviceCacheMgr;

    private final SocketIOServer server;

    @Autowired
    public TestwaScheduledRunner(SocketIOServer server) {
        this.server = server;
    }


    @Scheduled(cron = "0/10 * * * * ?")
    public void storeRunningLog() throws Exception {
        Long logSize = redisCacheManager.llen(WebsocketEvent.FB_RUNNGING_LOG);
        if(logSize == null || logSize == 0){
            return;
        }

        List<ProcedureInfo> logers = new ArrayList<>();
        for(int i=0;i < logSize; i++){
            try {
                ProcedureInfo procedure = (ProcedureInfo) redisCacheManager.rpop(WebsocketEvent.FB_RUNNGING_LOG, ProcedureInfo.class);
                String screenPath = procedure.getScreenshotPath();
                // 转换文件分隔符
                String configScreenPath = env.getProperty("screeshot.path");
                String[] pathsplit = screenPath.split("\\\\|/");
                String configScreenDirName = Paths.get(configScreenPath).getFileName().toString();
                String newScreenPath = Paths.get(configScreenDirName, pathsplit).toString();
                procedure.setScreenshotPath(newScreenPath);

                String encodeResult = new String(procedure.getAction().getBytes("UTF-8"),"UTF-8");
                log.info("encodeResult =====> {}", encodeResult);
                procedure.setAction(encodeResult);
                logers.add(procedure);
            }catch (Exception e){
                log.error("running log transfer error", e);
            }
        }
        mongoTemplate.insertAll(logers);

    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void checkDeviceOnline() throws Exception {
        // TODO
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