package com.testwa.distest.server.mongo.event;

import com.testwa.core.shell.UTF8CommonExecs;
import com.testwa.distest.config.DisFileProperties;
import com.testwa.distest.server.entity.LoggerFile;
import com.testwa.distest.server.mongo.model.CrashLog;
import com.testwa.distest.server.service.task.service.LoggerFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


/**
 * Created by wen on 19/08/2017.
 */
@Slf4j
@Component
public class LogcatAnalysisListener implements ApplicationListener<LogcatAnalysisEvent> {

    private static final String charact = "^\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d+ +\\d+ +\\d+ [FEWIDV] ";
    private static final String count = "count\\s+=\\s+(\\d+)\\s";
    private static final String proc_name = "proc_name\\s+=\\s+(\\S+)\\s";
    private static final String exception = "exception\\s+=\\s+(\\S+)";
    private static final String time = "^\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d+";
    private static final String newLogcatFile = "newLogcat.txt";
    private static final String outputFile = "output.txt";

    private final ExecutorService executorService = Executors.newFixedThreadPool(30);

    @Autowired
    private MongoOperations mongoTemplate;
    @Autowired
    private LoggerFileService loggerFileService;
    @Autowired
    private DisFileProperties disFileProperties;
    @Value("${dis-logdog.py}")
    private String logdogPy;

    @Override
    public void onApplicationEvent(LogcatAnalysisEvent e) {
        executorService.execute(() -> {

            log.info("logcat analysis start...");
            LoggerFile loggerFile = loggerFileService.findOne(e.getTaskId(), e.getDeviceId());
            Path logcatPath = Paths.get(disFileProperties.getLogcat(), loggerFile.buildPath());
            if(!Files.exists(logcatPath)){
                log.error("logcat file not found, {}", logcatPath.toString());
                return;
            }
            Path logcatDir = logcatPath.getParent();
            Path newLogcat = Paths.get(logcatDir.toString(), newLogcatFile);
            try {
                if(!Files.exists(newLogcat)){
                    Files.createFile(newLogcat);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            // 清理文件格式
            Pattern pattern = Pattern.compile(charact);
            try (Stream<String> stream = Files.lines(logcatPath, StandardCharsets.UTF_8); BufferedWriter writer = Files.newBufferedWriter(newLogcat)) {
                stream.forEach( l -> {
                    Matcher matcher = pattern.matcher(l);
                    if(matcher.find()){
                        try {
                            writer.write(l);
                            writer.write("\n");
                        } catch (IOException e1) {
                            log.error("write to output error, {}", newLogcat.toString(), e1);
                        }
                    }
                });
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                Files.move(newLogcat, logcatPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            // 执行
            Path outputLog = executeLogdog(logcatDir, newLogcat);

            // 分析outputlog文件
            // 清理文件格式
            List<CrashLog> logs = analysisResult(outputLog.toString());
            if(logs.size() > 0){
                logs.forEach( i -> {
                    i.setTaskId(e.getTaskId());
                    i.setDeviceId(e.getDeviceId());
                });
                // 存入数据库
                mongoTemplate.insertAll(logs);
            }

        });
    }

    private Path executeLogdog(Path logcatDir, Path newLogcat) {
        Path outputLog = Paths.get(logcatDir.toString(), outputFile);
        CommandLine commandLine = new CommandLine("python");
        commandLine.addArgument(logdogPy);
        commandLine.addArgument("-u");
        commandLine.addArgument(newLogcat.toString());
//            commandLine.addArgument("/Users/wen/dev/testWa/center/logcat.txt");
        commandLine.addArgument("-o");
        commandLine.addArgument(outputLog.toString());

        UTF8CommonExecs execs = new UTF8CommonExecs(commandLine);
        try {
            execs.exec();
        } catch (IOException e1) {
            String error = execs.getError();
            log.error("execut {} error {}", logdogPy, error, e1);
        }
        return outputLog;
    }


    private static List<CrashLog> analysisResult(String resultFile) {
        Path outputLog = Paths.get(resultFile);
        Pattern countPattern = Pattern.compile(count);
        Pattern procNamePattern = Pattern.compile(proc_name);
        Pattern exceptionPattern = Pattern.compile(exception);
        List<CrashLog> infos = new ArrayList<>();
        try {
            Long size = Files.size(outputLog);
            if(size > 0){
                try (Stream<String> stream = Files.lines(outputLog, StandardCharsets.UTF_8)) {
                    List<String> contents = new ArrayList<>();
                    StringBuffer sb = new StringBuffer();
                    stream.forEach( l -> {
                        l = l.trim();
                        if(!"====================================================================================================".equals(l)
                                && !"----------------------------------------------------------------------------------------------------".equals(l)){

                            Matcher matcher1 = countPattern.matcher(l);
                            if(matcher1.find()){
                                String count = matcher1.group(1);
                                String procName = "";
                                String exception = "";
                                Matcher matcher2 = procNamePattern.matcher(l);
                                if(matcher2.find()){
                                    procName = matcher2.group(1);
                                }
                                Matcher matcher3 = exceptionPattern.matcher(l);
                                if(matcher3.find()){
                                    exception = matcher3.group(1);
                                }
                                CrashLog info = new CrashLog();
                                info.setCount(count);
                                info.setProcName(procName);
                                info.setException(exception);
                                infos.add(info);
                                if(sb.length() > 0){
                                    contents.add(sb.toString());
                                    sb.delete(0, sb.length());
                                }
                            }else{
                                sb.append(l).append("\n");
                            }
                        }

                    });
                    contents.add(sb.toString());

                    for(int i=0;i<infos.size();i++){
                        CrashLog info = infos.get(i);
                        info.setContent(contents.get(i));
                        System.out.println(info.toString());
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (IOException e1) {
            log.error("get outputlog {} size error", outputLog.toString());
        }
        return infos;
    }


    public static void main(String[] args) {
        analysisResult("/Users/wen/dev/testWa/center/logcat/174/b110702/output.txt");
    }


}
