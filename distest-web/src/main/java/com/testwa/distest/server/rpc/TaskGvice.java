package com.testwa.distest.server.rpc;

import com.alibaba.fastjson.JSON;
import com.testwa.core.utils.ZipUtil;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.config.DisFileProperties;
import com.testwa.distest.server.entity.AppiumFile;
import com.testwa.distest.server.entity.LogFile;
import com.testwa.distest.server.entity.SubTask;
import com.testwa.distest.server.mongo.event.LogcatAnalysisEvent;
import com.testwa.distest.server.mongo.model.*;
import com.testwa.distest.server.mongo.service.MethodRunningLogService;
import com.testwa.distest.server.mongo.service.StepService;
import com.testwa.distest.server.mongo.service.TaskLogService;
import com.testwa.distest.server.mongo.service.TaskParamsService;
import com.testwa.distest.server.service.task.service.AppiumFileService;
import com.testwa.distest.server.service.task.service.LogFileService;
import com.testwa.distest.server.service.task.service.SubTaskService;
import com.testwa.distest.server.web.task.execute.PerformanceRedisMgr;
import com.testwa.distest.server.web.task.execute.ProcedureRedisMgr;
import com.testwa.distest.server.websocket.service.MessageNotifyService;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.task.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;

/**
 * Created by wen on 09/09/2017.
 */
@Slf4j
@GRpcService
public class TaskGvice extends TaskServiceGrpc.TaskServiceImplBase{
    private int mStatus = 200;
    private String mMessage = "";

    @Autowired
    private SubTaskService subTaskService;
    @Autowired
    private AppiumFileService appiumFileService;
    @Autowired
    private LogFileService loggerFileService;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private ProcedureRedisMgr procedureRedisMgr;
    @Autowired
    private PerformanceRedisMgr performanceRedisMgr;
    @Autowired
    private DisFileProperties disFileProperties;
    @Autowired
    private MethodRunningLogService executorLogInfoService;
    @Autowired
    private MessageNotifyService messageNotifyService;
    @Autowired
    private TaskLogService taskLoggerService;
    @Autowired
    private StepService stepService;
    @Autowired
    private TaskParamsService taskParamsService;

    @Override
    public void gameover(GameOverRequest request, StreamObserver<CommonReply> responseObserver) {
        Long taskCode = request.getTaskCode();
        Long timestamp = request.getTimestamp();
        String errorMessage = request.getErrorMessage();
        String deviceId = request.getDeviceId();

        SubTask subTask = subTaskService.findOne(taskCode, deviceId);
        if(subTask != null){
            subTask.setErrorMsg(errorMessage);
            subTask.setStatus(DB.TaskStatus.ERROR);
            subTask.setEndTime(new Date(timestamp));
            subTaskService.update(subTask);
        }else{
            log.error("exeTask info not format. {}", request.toString());
        }
        final CommonReply replyBuilder = CommonReply.newBuilder().setMessage("OK ").build();
        responseObserver.onNext(replyBuilder);
        responseObserver.onCompleted();
    }

    @Override
    public void missionComplete(MissionCompleteRequest request, StreamObserver<CommonReply> responseObserver) {
        Long exeId = request.getTaskCode();
        Long timestamp = request.getTimestamp();
        String deviceId = request.getDeviceId();
        SubTask subTask = subTaskService.findOne(exeId, deviceId);
        if(subTask != null){
            if(DB.TaskStatus.RUNNING.equals(subTask.getStatus())){
                subTask.setStatus(DB.TaskStatus.COMPLETE);
            }
            subTask.setEndTime(new Date(timestamp));
            subTaskService.update(subTask);
        }else{
            log.error("exeTask info not format. {}", request.toString());
        }
        final CommonReply replyBuilder = CommonReply.newBuilder().setMessage("OK ").build();
        responseObserver.onNext(replyBuilder);
        responseObserver.onCompleted();
    }

    @Override
    public void appiumRunningLogUpload(AppiumRunningLogUploadRequest request, StreamObserver<CommonReply> responseObserver) {
        String info = request.getInfoJson();
        procedureRedisMgr.addProcedureToQueue(info);

        final CommonReply replyBuilder = CommonReply.newBuilder().setMessage("OK ").build();
        responseObserver.onNext(replyBuilder);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<FileUploadRequest> logcatFileUpload(StreamObserver<CommonReply> responseObserver) {
        return new StreamObserver<FileUploadRequest>() {
            private BufferedOutputStream mBufferedOutputStream = null;
            int mmCount = 0;
            String deviceId = "";
            Long taskCode = null;

            @Override
            public void onNext(FileUploadRequest request) {
                log.debug("onNext count: " + mmCount);
                mmCount++;

                byte[] data = request.getData().toByteArray();
                int offset = request.getOffset();
                int size = (int) request.getSize();
                String name = request.getName();
                deviceId = request.getDeviceId();
                taskCode = request.getTaskCode();

                LogFile file = new LogFile();
                file.setFilename(name);
                file.setTaskCode(taskCode);
                file.setDeviceId(deviceId);
                file.setCreateTime(new Date());
                LogFile oldFile = loggerFileService.findOne(taskCode, deviceId);
                if(oldFile == null){
                    loggerFileService.save(file);
                }

                String localPath = disFileProperties.getLogcat();
                Path localFile = Paths.get(localPath, file.buildPath());
                if(!Files.exists(localFile.getParent())){
                    try {
                        Files.createDirectories(localFile.getParent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(!Files.exists(localFile)){
                    try {
                        Files.createFile(localFile);
                    } catch (IOException e) {
                        log.error("Receive logcat file, create error", e);
                    }
                }
                try {
                    if (mBufferedOutputStream == null) {
                        mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localFile.toFile()));
                    }
                    mBufferedOutputStream.write(data, 0, data.length);
                    mBufferedOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(CommonReply.newBuilder()
                        .setStatus(mStatus)
                        .setMessage(mMessage)
                        .build());
                responseObserver.onCompleted();
                if (mBufferedOutputStream != null) {
                    try {
                        mBufferedOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mBufferedOutputStream = null;
                    }
                }
                context.publishEvent(new LogcatAnalysisEvent(this, deviceId, taskCode));
            }
        };
    }

    @Override
    public StreamObserver<FileUploadRequest> appiumLogFileUpload(StreamObserver<CommonReply> responseObserver) {
        return new StreamObserver<FileUploadRequest>() {
            private BufferedOutputStream mBufferedOutputStream = null;
            int mmCount = 0;

            @Override
            public void onNext(FileUploadRequest request) {
                log.debug("onNext count: " + mmCount);
                mmCount++;

                byte[] data = request.getData().toByteArray();
                int offset = request.getOffset();
                int size = (int) request.getSize();
                String name = request.getName();
                String deviceId = request.getDeviceId();
                long taskCode = request.getTaskCode();

                AppiumFile appiumFile = new AppiumFile();
                appiumFile.setFilename(name);
                appiumFile.setTaskCode(taskCode);
                appiumFile.setDeviceId(deviceId);
                appiumFile.setCreateTime(new Date());
                AppiumFile oldFile = appiumFileService.findOne(taskCode, deviceId);
                if(oldFile == null){
                    appiumFileService.save(appiumFile);
                }

                String localPath = disFileProperties.getAppium();
                Path localFile = Paths.get(localPath, appiumFile.buildPath());
                if(!Files.exists(localFile.getParent())){
                    try {
                        Files.createDirectories(localFile.getParent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(!Files.exists(localFile)){
                    try {
                        Files.createFile(localFile);
                    } catch (IOException e) {
                        log.error("Receive appium log, create error", e);
                    }
                }
                try {
                    if (mBufferedOutputStream == null) {
                        mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localFile.toFile()));
                    }
                    mBufferedOutputStream.write(data, 0, size);
                    mBufferedOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(CommonReply.newBuilder()
                        .setStatus(mStatus)
                        .setMessage(mMessage)
                        .build());
                responseObserver.onCompleted();
                if (mBufferedOutputStream != null) {
                    try {
                        mBufferedOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mBufferedOutputStream = null;
                    }
                }
            }
        };
    }

    @Override
    public StreamObserver<FileUploadRequest> imgFileUpload(StreamObserver<CommonReply> responseObserver) {
        return new StreamObserver<FileUploadRequest>() {
            private BufferedOutputStream mBufferedOutputStream = null;

            @Override
            public void onNext(FileUploadRequest request) {
                byte[] data = request.getData().toByteArray();
                String name = request.getName();
                Long taskCode = request.getTaskCode();
                String deviceId = request.getDeviceId();
                String localPath = disFileProperties.getScreeshot();
                Path localFile = Paths.get(localPath, String.valueOf(taskCode), deviceId, name);
                if(!Files.exists(localFile.getParent())){
                    try {
                        Files.createDirectories(localFile.getParent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(!Files.exists(localFile)){
                    try {
                        Files.createFile(localFile);
                    } catch (IOException e) {
                        log.error("Receive screen img, create error", e);
                    }
                }
                try {
                    if (mBufferedOutputStream == null) {
                        mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localFile.toFile()));
                    }
                    mBufferedOutputStream.write(data);
                    mBufferedOutputStream.flush();
                } catch (Exception e) {
                    log.error("register img error", e);
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(CommonReply.newBuilder()
                        .setStatus(mStatus)
                        .setMessage(mMessage)
                        .build());
                responseObserver.onCompleted();
                if (mBufferedOutputStream != null) {
                    try {
                        mBufferedOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mBufferedOutputStream = null;
                    }
                }
            }
        };
    }


    @Override
    public StreamObserver<FileUploadRequest> zipFileUpload(StreamObserver<CommonReply> responseObserver) {
        return new StreamObserver<FileUploadRequest>() {
            private BufferedOutputStream mBufferedOutputStream = null;
            int mmCount = 0;
            Path localZipFile;

            @Override
            public void onNext(FileUploadRequest request) {
                log.debug("onNext count: " + mmCount);
                mmCount++;

                byte[] data = request.getData().toByteArray();
                int offset = request.getOffset();
                int size = (int) request.getSize();
                String name = request.getName();
                long taskCode = request.getTaskCode();
                String deviceId = request.getDeviceId();
                String localPath = disFileProperties.getScreeshot();
                localZipFile = Paths.get(localPath, String.valueOf(taskCode), deviceId, name);
                if(!Files.exists(localZipFile.getParent())){
                    try {
                        Files.createDirectories(localZipFile.getParent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(!Files.exists(localZipFile)){
                    try {
                        Files.createFile(localZipFile);
                    } catch (IOException e) {
                        log.error("Receive zip file, create error", e);
                    }
                }
                try {
                    if (mBufferedOutputStream == null) {
                        mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localZipFile.toFile()));
                    }
                    mBufferedOutputStream.write(data, 0, size);
                    mBufferedOutputStream.flush();
                } catch (Exception e) {
                    log.error("offset: {}, size: {}", offset, size);
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(CommonReply.newBuilder()
                        .setStatus(mStatus)
                        .setMessage(mMessage)
                        .build());
                responseObserver.onCompleted();
                if (mBufferedOutputStream != null) {
                    try {
                        mBufferedOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mBufferedOutputStream = null;
                    }
                }
                try {
                    ZipUtil.unZipFiles(localZipFile.toFile(), localZipFile.getParent().toString());
                    Files.deleteIfExists(localZipFile);
                } catch (IOException e) {

                }
            }
        };
    }


    @Override
    public StreamObserver<FileUploadRequest> fileUpload(StreamObserver<CommonReply> responseObserver) {
        return new StreamObserver<FileUploadRequest>() {
            private BufferedOutputStream mBufferedOutputStream = null;
            int mmCount = 0;
            Path localFile;

            @Override
            public void onNext(FileUploadRequest request) {
                mmCount++;

                byte[] data = request.getData().toByteArray();
                String name = request.getName();
                FileUploadRequest.Type type = request.getType();
                long taskCode = request.getTaskCode();
                String deviceId = request.getDeviceId();
                int offset = request.getOffset();
                int kbSize = request.getSize();  // KB
                String localPath = disFileProperties.getDist();

                String fileRelativePath = Paths.get(String.valueOf(taskCode), deviceId, type.name(), name).toString();
                localFile = Paths.get(localPath, fileRelativePath);
                if(!Files.exists(localFile.getParent())){
                    try {
                        Files.createDirectories(localFile.getParent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(!Files.exists(localFile)){
                    try {
                        Files.createFile(localFile);
                    } catch (IOException e) {
                        log.error("Receive file, create error", e);
                    }
                }

                SubTask subTask = subTaskService.findOne(taskCode, deviceId);
                if(subTask != null && FileUploadRequest.Type.video.equals(type)) {
                    if(StringUtils.isBlank(subTask.getVideo())) {
                        subTaskService.updateVideoPath(taskCode, deviceId, fileRelativePath);
                    }
                }

                try {
                    if (mBufferedOutputStream == null) {
                        mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localFile.toFile(), true));
                    }
                    mBufferedOutputStream.write(data);
                    mBufferedOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(CommonReply.newBuilder()
                        .setStatus(mStatus)
                        .setMessage(mMessage)
                        .build());
                responseObserver.onCompleted();
                if (mBufferedOutputStream != null) {
                    try {
                        mBufferedOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mBufferedOutputStream = null;
                    }
                }
            }
        };
    }


    @Override
    public void executorLogUpload(ExecutorLogRequest request, StreamObserver<CommonReply> responseObserver){

        String token = request.getToken();
        Long timestamp = request.getTimestamp();
        Long taskCode = request.getTaskCode();
        String deviceId = request.getDeviceId();
        String desc = request.getMethodDesc();
        int actionOrder = request.getOrder();
        String args = request.getArgs();
        String flag = request.getFlag();
        String methodName = request.getMethodName();


        MethodRunningLog logInfo = new MethodRunningLog();
        logInfo.setMethodDesc(desc);
        logInfo.setArgs(args);
        logInfo.setDeviceId(deviceId);
        logInfo.setMethodName(methodName);
        logInfo.setToken(token);
        logInfo.setTaskCode(taskCode);
        logInfo.setTimestamp(timestamp);
        logInfo.setFlag(flag);
        logInfo.setMethodOrder(actionOrder);

        executorLogInfoService.save(logInfo);

        // 通知页面
        messageNotifyService.taskAction(logInfo);

        final CommonReply replyBuilder = CommonReply.newBuilder().setMessage("OK ").build();
        responseObserver.onNext(replyBuilder);
        responseObserver.onCompleted();

    }

    /**
     *@Description: logcat字符串上传接口
     *@Param: [request, responseObserver]
     *@Return: void
     *@Author: wen
     *@Date: 2018/5/16
     */
    @Override
    public void logcatStrUpload(LogcatStrRequest request, StreamObserver<CommonReply> responseObserver){
        Long taskCode = request.getTaskCode();
        String deviceId = request.getDeviceId();
        String content = request.getContent();
        Long timestamp = request.getTimestamp();
        TaskLog log = new TaskLog();
        log.setContent(content);
        log.setDeviceId(deviceId);
        log.setTaskCode(taskCode);
        log.setTimestamp(timestamp);
        taskLoggerService.save(log);

        final CommonReply replyBuilder = CommonReply.newBuilder().setMessage("OK ").build();
        responseObserver.onNext(replyBuilder);
        responseObserver.onCompleted();
    }

    @Override
    public void saveStep(StepRequest request, StreamObserver<CommonReply> responseObserver){
        Long taskCode = request.getTaskCode();
        String deviceId = request.getDeviceId();
        String img = request.getImg();
        String dump = request.getDump();
        Long timestamp = request.getTimestamp();
        Long testcaseId = request.getTestcaseId();
        Long scriptId = request.getScriptId();
        String commandParams = request.getCommadParams();
        String commadAction = request.getCommadAction();
        String value = request.getValue();
        String sessionId = request.getSessionId();

        Step step = new Step();
        step.setDeviceId(deviceId);
        step.setTaskCode(taskCode);
        step.setDump(dump);
        step.setImg(img);
        step.setImgBefore(request.getImgBefore());
        step.setAction(request.getAction().name());
        step.setOrder(request.getAction().getNumber());
        step.setRuntime(request.getRuntime());
        step.setStatus(request.getStatus().getNumber());
        step.setErrormsg(request.getErrormsg());

        step.setTestcaseId(testcaseId);
        step.setScriptId(scriptId);
        step.setTimestamp(timestamp);
        step.setCommandAction(commadAction);
        step.setCommandParams(commandParams);
        step.setValue(value);
        step.setSessionId(sessionId);

        stepService.save(step);

        final CommonReply replyBuilder = CommonReply.newBuilder().setMessage("OK ").build();
        responseObserver.onNext(replyBuilder);
        responseObserver.onCompleted();
    }

    @Override
    public void savePerformance(PerformanceRequest request, StreamObserver<CommonReply> responseObserver){
        Performance performance = new Performance();
        performance.setDeviceId(request.getDeviceId());
        performance.setTaskCode(request.getTaskCode());
        performance.setBat(request.getBat());
        performance.setCpu(request.getCpu());
        performance.setFps(request.getFps());
        performance.setGprsDown(request.getGprsDown());
        performance.setGprsUp(request.getGprsUp());
        performance.setWifiDown(request.getWifiDown());
        performance.setWifiUp(request.getWifiUp());
        performance.setMem(request.getMem());
        performance.setTimestamp(request.getTimestamp());

        performanceRedisMgr.addPerformanceToQueue(performance);
        final CommonReply replyBuilder = CommonReply.newBuilder().setMessage("OK ").build();
        responseObserver.onNext(replyBuilder);
        responseObserver.onCompleted();
    }

    @Override
    public void taskConfig(TaskCodeRequest request, StreamObserver<CommonReply> responseObserver) {
        TaskParams params = taskParamsService.findOneByTaskCode(request.getTaskCode());
        if(params != null) {
            Map<String, Object> config = params.getParams();
            final CommonReply replyBuilder = CommonReply.newBuilder().setStatus(1).setMessage(JSON.toJSONString(config)).build();
            responseObserver.onNext(replyBuilder);
        }else{
            final CommonReply replyBuilder = CommonReply.newBuilder().setStatus(0).setMessage("FAIL ").build();
            responseObserver.onNext(replyBuilder);
        }
        responseObserver.onCompleted();
    }
}
