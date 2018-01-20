package com.testwa.distest.server.rpc.service;

<<<<<<< HEAD
import com.testwa.distest.server.LogInterceptor;
import com.testwa.distest.server.mvc.event.GameOverEvent;
import com.testwa.distest.server.mvc.model.ExecutionTask;
import com.testwa.distest.server.mvc.service.ExeTaskService;
import com.testwa.distest.server.mvc.service.TaskService;
import com.testwa.distest.server.mvc.service.cache.RemoteClientService;
=======
import com.testwa.core.utils.DateUtils;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.config.DisFileProperties;
import com.testwa.distest.config.security.JwtTokenUtil;
import com.testwa.distest.server.entity.AppiumFile;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.LogInterceptor;
import com.testwa.distest.server.mvc.event.GameOverEvent;
>>>>>>> mysql-beta-2.2.0
import com.testwa.distest.server.rpc.GRpcService;
import com.testwa.distest.server.service.cache.mgr.TaskCacheMgr;
import com.testwa.distest.server.service.task.service.AppiumFileService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.web.task.execute.ProcedureRedisMgr;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.task.*;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
@GRpcService(interceptors = { LogInterceptor.class })
public class TaskGvice extends TaskServiceGrpc.TaskServiceImplBase{

    private BufferedOutputStream mBufferedOutputStream = null;
    private int mStatus = 200;
    private String mMessage = "";

    @Autowired
    private TaskService taskService;
    @Autowired
<<<<<<< HEAD
    private ExeTaskService exeTaskService;
=======
    private AppiumFileService appiumFileService;
    @Autowired
    private UserService userService;
>>>>>>> mysql-beta-2.2.0
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

    @Override
    public void gameover(TaskOverRequest request, StreamObserver<CommonReply> responseObserver) {
        Long exeId = request.getExeId();
        Long timestamp = request.getTimestamp();

<<<<<<< HEAD
        ExecutionTask exeTask = exeTaskService.getExeTaskById(exeId);
        if(exeTask != null && exeTask.getCreator().equals(userId)){
            if(exeTask.getStatus() != ExecutionTask.StatusEnum.CANCEL.getCode()){
                exeTask.setStatus(ExecutionTask.StatusEnum.STOP.getCode());
=======
        Task exeTask = taskService.findOne(exeId);
        if(exeTask != null){
            if(exeTask.getStatus().getValue() != DB.TaskStatus.CANCEL.getValue()){
                exeTask.setStatus(DB.TaskStatus.COMPLETE);
>>>>>>> mysql-beta-2.2.0
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
                log.info("onNext count: " + mmCount);
                mmCount++;

                byte[] data = request.getData().toByteArray();
                int offset = request.getOffset();
                String name = request.getName();

                try {
                    if (mBufferedOutputStream == null) {
                        mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream("receive_" + name));
                    }
                    mBufferedOutputStream.write(data, offset, data.length);
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
                log.info("onNext count: " + mmCount);
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
                    mBufferedOutputStream.write(data, offset, size);
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
                log.info("onNext count: " + mmCount);
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
                    mBufferedOutputStream.write(data, offset, size);
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
}
