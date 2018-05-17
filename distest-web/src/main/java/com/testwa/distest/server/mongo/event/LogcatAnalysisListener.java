package com.testwa.distest.server.mongo.event;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.testwa.distest.config.DisFileProperties;
import com.testwa.distest.server.entity.LoggerFile;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.mongo.model.ProcedureInfo;
import com.testwa.distest.server.mongo.model.ProcedureStatis;
import com.testwa.distest.server.mongo.service.ProcedureInfoService;
import com.testwa.distest.server.service.task.service.LoggerFileService;
import com.testwa.distest.server.service.task.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;


/**
 * Created by wen on 19/08/2017.
 */
@Slf4j
@Component
public class LogcatAnalysisListener implements ApplicationListener<LogcatAnalysisEvent> {

    private static final String NullPointer="java.lang.NullPointerException";
    private static final String IllegalState="java.lang.IllegalStateException";
    private static final String IllegalArgument="java.lang.IllegalArgumentException";
    private static final String ArrayIndexOutOfBounds="java.lang.ArrayIndexOutOfBoundsException";
    private static final String RuntimeException="java.lang.RuntimeException";
    private static final String SecurityException="java.lang.SecurityException";

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Autowired
    private MongoOperations mongoTemplate;
    @Autowired
    private LoggerFileService loggerFileService;
    @Autowired
    private DisFileProperties disFileProperties;

    @Async
    @Override
    public void onApplicationEvent(LogcatAnalysisEvent e) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                log.info("logcat analysis start...");
                LoggerFile loggerFile = loggerFileService.findOne(e.getTaskId(), e.getDeviceId());
                Path logcatPath = Paths.get(disFileProperties.getLogcat(), loggerFile.buildPath());
                if(!Files.exists(logcatPath)){
                    log.error("logcat file not found, {}", logcatPath.toString());
                    return;
                }
                try ( Stream<String> stream = Files.lines(logcatPath, StandardCharsets.UTF_8) ) {
                    stream.forEach( l -> {
//                        log.info(l);
                    });
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });


    }

}
