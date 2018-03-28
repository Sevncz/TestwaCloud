package com.testwa.distest.server.service.rpc;

import com.testwa.core.utils.DateUtils;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.config.DisFileProperties;
import com.testwa.distest.config.security.JwtTokenUtil;
import com.testwa.distest.server.entity.AppiumFile;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.mongo.event.GameOverEvent;
import com.testwa.distest.server.mongo.model.ExecutorLogInfo;
import com.testwa.distest.server.mongo.service.ExecutorLogInfoService;
import com.testwa.distest.server.service.cache.mgr.TaskCacheMgr;
import com.testwa.distest.server.service.task.service.AppiumFileService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.web.task.execute.ProcedureRedisMgr;
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
    private TaskService taskService;
    @Autowired
    private AppiumFileService appiumFileService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private TaskCacheMgr taskCacheMgr;
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

    @Override
    public void gameover(TaskOverRequest request, StreamObserver<CommonReply> responseObserver) {
        Long exeId = request.getExeId();
        Long timestamp = request.getTimestamp();

        Task exeTask = taskService.findOne(exeId);
        if(exeTask != null){
            if(exeTask.getStatus().getValue() != DB.TaskStatus.CANCEL.getValue()){
                exeTask.setStatus(DB.TaskStatus.COMPLETE);
            }
            exeTask.setEndTime(DateUtils.getMongoDate(new Date(timestamp)));
            taskService.update(exeTask);
            exeTask.getDevices().forEach(d -> {
                deviceAuthMgr.releaseDev(d.getDeviceId());
            });
            context.publishEvent(new GameOverEvent(this, request.getExeId()));
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
    public StreamObserver<FileUploadRequest> logcatUpload(StreamObserver<CommonReply> responseObserver) {
        return new StreamObserver<FileUploadRequest>() {
            int mmCount = 0;

            @Override
            public void onNext(FileUploadRequest request) {
                log.debug("onNext count: " + mmCount);
                mmCount++;

                byte[] data = request.getData().toByteArray();
                int offset = request.getOffset();
                String name = request.getName();

                try {
                    if (mBufferedOutputStream == null) {
                        mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream("receive_" + name));
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
            }
        };
    }

    @Override
    public StreamObserver<FileUploadRequest> appiumLogUpload(StreamObserver<CommonReply> responseObserver) {
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
    public StreamObserver<FileUploadRequest> imgUpload(StreamObserver<CommonReply> responseObserver) {
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
    }

}
