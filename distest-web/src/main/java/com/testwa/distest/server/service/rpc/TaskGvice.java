package com.testwa.distest.server.service.rpc;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.config.DisFileProperties;
import com.testwa.distest.config.security.JwtTokenUtil;
import com.testwa.distest.server.entity.AppiumFile;
import com.testwa.distest.server.entity.LoggerFile;
import com.testwa.distest.server.entity.TaskDevice;
import com.testwa.distest.server.mongo.event.LogcatAnalysisEvent;
import com.testwa.distest.server.mongo.model.ExecutorLogInfo;
import com.testwa.distest.server.mongo.model.TaskLogger;
import com.testwa.distest.server.mongo.service.ExecutorLogInfoService;
import com.testwa.distest.server.mongo.service.TaskLoggerService;
import com.testwa.distest.server.service.cache.mgr.TaskCacheMgr;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.task.service.AppiumFileService;
import com.testwa.distest.server.service.task.service.LoggerFileService;
import com.testwa.distest.server.service.task.service.TaskDeviceService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.web.task.execute.ProcedureRedisMgr;
import com.testwa.distest.server.websocket.service.MessageNotifyService;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.task.*;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Created by wen on 09/09/2017.
 */
@Slf4j
@GRpcService
public class TaskGvice extends TaskServiceGrpc.TaskServiceImplBase{
    private BufferedOutputStream mBufferedOutputStream = null;
    private int mStatus = 200;
    private String mMessage = "";

    @Autowired
    private TaskDeviceService taskDeviceService;
    @Autowired
    private AppiumFileService appiumFileService;
    @Autowired
    private LoggerFileService loggerFileService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private TaskCacheMgr taskCacheMgr;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private ProcedureRedisMgr procedureRedisMgr;
    @Autowired
    private DisFileProperties disFileProperties;
    @Autowired
    private ExecutorLogInfoService executorLogInfoService;
    @Autowired
    private MessageNotifyService messageNotifyService;
    @Autowired
    private TaskLoggerService taskLoggerService;

    @Override
    public void gameover(GameOverRequest request, StreamObserver<CommonReply> responseObserver) {
        Long exeId = request.getExeId();
        Long timestamp = request.getTimestamp();
        String errorMessage = request.getErrorMessage();
        String deviceId = request.getDeviceId();

        TaskDevice exeTask = taskDeviceService.findOne(exeId, deviceId);
        if(exeTask != null){
            exeTask.setErrorMsg(errorMessage);
            exeTask.setStatus(DB.TaskStatus.ERROR);
            exeTask.setEndTime(new Date(timestamp));
            taskDeviceService.update(exeTask);
        }else{
            log.error("exeTask info not format. {}", request.toString());
        }
    }

    @Override
    public void missionComplete(MissionCompleteRequest request, StreamObserver<CommonReply> responseObserver) {
        Long exeId = request.getExeId();
        Long timestamp = request.getTimestamp();
        String deviceId = request.getDeviceId();
        TaskDevice exeTask = taskDeviceService.findOne(exeId, deviceId);
        if(exeTask != null){
            if(DB.TaskStatus.RUNNING.equals(exeTask.getStatus())){
                exeTask.setStatus(DB.TaskStatus.COMPLETE);
            }
            exeTask.setEndTime(new Date(timestamp));
            taskDeviceService.update(exeTask);
        }else{
            log.error("exeTask info not format. {}", request.toString());
        }
    }

    @Override
    public void currExeInfo(CurrentExeInfoRequest request, StreamObserver<CommonReply> responseObserver){

        Long exeId = request.getExeId();
        String deviceId = request.getDeviceId();
        Long scriptId = request.getScriptId();
        Long testcaseId = request.getTestcaseId();

        taskCacheMgr.saveExeInfo(exeId, deviceId, testcaseId, scriptId);

    }

    @Override
    public void procedureInfoUpload(ProcedureInfoUploadRequest request, StreamObserver<CommonReply> responseObserver) {
        String info = request.getInfoJson();
        procedureRedisMgr.addProcedureToQueue(info);
    }

    @Override
    public StreamObserver<FileUploadRequest> logcatFileUpload(StreamObserver<CommonReply> responseObserver) {
        return new StreamObserver<FileUploadRequest>() {
            int mmCount = 0;
            String deviceId = "";
            Long taskId = null;

            @Override
            public void onNext(FileUploadRequest request) {
                log.debug("onNext count: " + mmCount);
                mmCount++;

                byte[] data = request.getData().toByteArray();
                int offset = request.getOffset();
                int size = (int) request.getSize();
                String name = request.getName();
                deviceId = request.getDeviceId();
                taskId = request.getExeId();

                LoggerFile file = new LoggerFile();
                file.setFilename(name);
                file.setTaskId(taskId);
                file.setDeviceId(deviceId);
                file.setCreateTime(new Date());
                LoggerFile oldFile = loggerFileService.findOne(taskId, deviceId);
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
                context.publishEvent(new LogcatAnalysisEvent(this, deviceId, taskId));
            }
        };
    }

    @Override
    public StreamObserver<FileUploadRequest> appiumLogFileUpload(StreamObserver<CommonReply> responseObserver) {
        return new StreamObserver<FileUploadRequest>() {
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
                long taskId = request.getExeId();

                AppiumFile appiumFile = new AppiumFile();
                appiumFile.setFilename(name);
                appiumFile.setTaskId(taskId);
                appiumFile.setDeviceId(deviceId);
                appiumFile.setCreateTime(new Date());
                AppiumFile oldFile = appiumFileService.findOne(taskId, deviceId);
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
            int mmCount = 0;

            @Override
            public void onNext(FileUploadRequest request) {
                log.debug("onNext count: " + mmCount);
                mmCount++;

                byte[] data = request.getData().toByteArray();
                int offset = request.getOffset();
                int size = (int) request.getSize();
                String name = request.getName();
                long taskId = request.getExeId();
                String localPath = disFileProperties.getScreeshot();
                Path localFile = Paths.get(localPath, taskId+"", name);
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
            }
        };
    }


    @Override
    public void executorLogUpload(ExecutorLogRequest request, StreamObserver<CommonReply> responseObserver){

        String token = request.getToken();
        Long timestamp = request.getTimestamp();
        Long taskId = request.getExeId();
        String deviceId = request.getDeviceId();
        String action = request.getAction();
        int actionOrder = request.getOrder();
        String args = request.getArgs();
        String flag = request.getFlag();
        String methodName = request.getMethodName();


        ExecutorLogInfo logInfo = new ExecutorLogInfo();
        logInfo.setAction(action);
        logInfo.setArgs(args);
        logInfo.setDeviceId(deviceId);
        logInfo.setMethodName(methodName);
        logInfo.setToken(token);
        logInfo.setTaskId(taskId);
        logInfo.setTimestamp(timestamp);
        logInfo.setFlag(flag);
        logInfo.setActionOrder(actionOrder);

        executorLogInfoService.save(logInfo);

        // 通知页面
        messageNotifyService.taskAction(logInfo);

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
        Long taskId = request.getExeId();
        String deviceId = request.getDeviceId();
        String content = request.getContent();
        Long timestamp = request.getTimestamp();
        TaskLogger logger = new TaskLogger();
        logger.setContent(content);
        logger.setDeviceId(deviceId);
        logger.setTaskId(taskId);
        logger.setTimestamp(timestamp);
        taskLoggerService.save(logger);
    }

}
