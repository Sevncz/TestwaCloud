package com.testwa.distest.client.service;import com.alibaba.fastjson.JSON;import com.github.cosysoft.device.android.AndroidDevice;import com.google.common.util.concurrent.ListenableFuture;import com.google.protobuf.ByteString;import com.testwa.core.utils.TimeUtil;import com.testwa.distest.client.DeviceClient;import com.testwa.distest.client.DeviceClientCache;import com.testwa.distest.client.android.AndroidHelper;import com.testwa.distest.client.component.FlowResult;import com.testwa.distest.client.exception.DeviceNotReadyException;import com.testwa.distest.client.model.ExecutorLogInfo;import com.testwa.distest.client.model.IOSDevice;import com.testwa.distest.client.model.UserInfo;import com.testwa.distest.client.component.Constant;import io.grpc.ManagedChannel;import io.grpc.stub.StreamObserver;import io.rpc.testwa.device.DeviceStatusChangeRequest;import io.rpc.testwa.task.*;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.beans.factory.annotation.Value;import org.springframework.scheduling.annotation.Async;import org.springframework.stereotype.Component;import java.io.*;import java.nio.channels.FileChannel;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.HashMap;import java.util.Map;import java.util.concurrent.ExecutionException;@Slf4j@Componentpublic class GrpcClientService {    @Value("${distest.agent.resources}")    private String resourcesPath;    @Value("${cloud.socket.url}")    private String socketUrl;    @Value("${username}")    private String username;    @Autowired    private ManagedChannel serverChannel;    @Async    public void initDevice(String deviceId) throws DeviceNotReadyException {        if(StringUtils.isBlank(deviceId)){            log.error("deviceId is null, init device client error ");            return;        }        // 注册服务        DeviceClient dc = DeviceClientCache.get(deviceId);        if(dc != null){            return;        }        AndroidDevice ad = AndroidHelper.getInstance().getAndroidDevice(deviceId);        if(ad != null && ad.getDevice().isOnline()){            dc = new DeviceClient(deviceId, serverChannel, resourcesPath, username);            DeviceClientCache.add(deviceId, dc);            dc.registerToServer();        }else{            log.info("设备 {} 处于离线状态", deviceId);        }    }    @Async    public void initDevice(IOSDevice d) {//        log.debug("iOS 设备 " + d.getDeviceId() + " 通知服务器初始化");    }    @Async    public void procedureInfoUpload(String urlInfo) {        if(!StringUtils.isEmpty(urlInfo)){            Map<String, Object> payload = JSON.parseObject(urlInfo);            if(payload == null){                payload = new HashMap<>();            }            payload.put("timestamp", TimeUtil.getTimestampLong());            payload.put("createDate", TimeUtil.getTimestamp());            urlInfo = JSON.toJSONString(payload);            AppiumRunningLogUploadRequest procedureInfoUploadRequest = AppiumRunningLogUploadRequest.newBuilder()                    .setInfoJson(urlInfo)                    .build();            Gvice.taskService(serverChannel).appiumRunningLogUpload(procedureInfoUploadRequest);            String screenshotPath = (String) payload.get("screenshotPath");            if(StringUtils.isNotEmpty(screenshotPath)){                Long exeId = Long.parseLong((String) payload.get("executionTaskId"));                String deviceId = (String) payload.get("deviceId");                Path imgPath = Paths.get(Constant.localScreenshotPath, screenshotPath);                log.info("上传截图, {}", imgPath.toString());                imgUpload(exeId, imgPath.toString(), deviceId);            }        }    }    public void notifyServerCurrentTaskExecutorInfo(String deviceId, Long taskId, Long currScriptId, Long currTestcaseId) {        CurrentExeInfoRequest request = CurrentExeInfoRequest.newBuilder()                .setDeviceId(deviceId)                .setExeId(taskId)                .setScriptId(currScriptId)                .setTestcaseId(currTestcaseId)                .setToken(UserInfo.token)                .build();        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).currExeInfo(request);        try {            CommonReply c = reply.get();            log.info(c.getMessage());        } catch (InterruptedException | ExecutionException e) {            log.error("任务执行情况通知失败", e);        }finally {        }    }    /**     *@Description: 测试任务失败     *@Param: [exeId, errorMessage]     *@Return: void     *@Author: wen     *@Date: 2018/5/2     */    public void gameover(Long exeId, String deviceId, String errorMessage) {        GameOverRequest request = GameOverRequest.newBuilder()                .setToken(UserInfo.token)                .setExeId(exeId)                .setDeviceId(deviceId)                .setErrorMessage(errorMessage)                .setTimestamp(TimeUtil.getTimestampLong())                .build();        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).gameover(request);        try {            CommonReply c = reply.get();            log.info(c.getMessage());        } catch (InterruptedException | ExecutionException e) {            log.error("任务执行失败通知失败", e);        }finally {        }    }    /**     *@Description: 测试任务完成     *@Param: [exeId]     *@Return: void     *@Author: wen     *@Date: 2018/5/2     */    public void missionComplete(Long exeId, String deviceId) {        MissionCompleteRequest request = MissionCompleteRequest.newBuilder()                .setToken(UserInfo.token)                .setExeId(exeId)                .setDeviceId(deviceId)                .setTimestamp(TimeUtil.getTimestampLong())                .build();        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).missionComplete(request);        try {            CommonReply c = reply.get();            log.info(c.getMessage());        } catch (InterruptedException | ExecutionException e) {            log.error("任务执行成功通知失败", e);        }finally {        }    }    @Async    public void appiumLogUpload(Long exeId, String deviceId, String appiumLogPath) {        log.debug("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.debug("Client response onNext");            }            @Override            public void onError(Throwable t) {                log.debug("Client response onError");            }            @Override            public void onCompleted() {                log.debug("Client response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).appiumLogFileUpload(responseObserver);        try {            File file = new File(appiumLogPath);            if (!file.exists()) {                log.error("Appium日志文件不存在, {}", appiumLogPath);                return;            }            String filename = file.getName();            try {                FileInputStream fis = new FileInputStream(file);                BufferedInputStream bInputStream = new BufferedInputStream(fis);                FileChannel fc = fis.getChannel();                long total = fc.size();                int bufferSize = 100 * 1024;                byte[] buffer = new byte[bufferSize];                int offset = 0;                int size = 0;                while ((size = bInputStream.read(buffer)) > 0) {                    ByteString byteString = ByteString.copyFrom(buffer);                    FileUploadRequest req = FileUploadRequest.newBuilder()                            .setName(filename)                            .setData(byteString)                            .setSize(size)                            .setOffset(offset)                            .setExeId(exeId)                            .setDeviceId(deviceId)                            .setTimestamp(TimeUtil.getTimestampLong())                            .setToken(UserInfo.token)                            .build();                    requestObserver.onNext(req);                    offset += size;                }            } catch (IOException e) {                e.printStackTrace();            }        } catch (RuntimeException e) {            requestObserver.onError(e);            throw e;        }        requestObserver.onCompleted();    }    public void imgUpload(Long exeId, String imgFile, String deviceId) {        log.debug("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.debug("Client img response onNext");            }            @Override            public void onError(Throwable t) {                log.debug("Client img response onError");            }            @Override            public void onCompleted() {                log.debug("Client img response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).imgFileUpload(responseObserver);        try {            Path imgPath = Paths.get(imgFile);            File file = imgPath.toFile();            if (!file.exists()) {                log.error("截图文件不存在, {}", imgPath);                return;            }            log.debug("路径：{}  文件大小: {}", imgPath.toString(), Files.size(imgPath));            String filename = file.getName();            byte[] contentBytes = Files.readAllBytes(imgPath);            ByteString byteString = ByteString.copyFrom(contentBytes);            FileUploadRequest req = FileUploadRequest.newBuilder()                    .setName(filename)                    .setData(byteString)                    .setSize(Files.size(imgPath))                    .setOffset(0)                    .setExeId(exeId)                    .setDeviceId(deviceId)                    .setTimestamp(TimeUtil.getTimestampLong())                    .setToken(UserInfo.token)                    .build();            requestObserver.onNext(req);        } catch (RuntimeException | IOException e) {            requestObserver.onError(e);            log.error("上传文件失败", e);        } finally {            requestObserver.onCompleted();        }    }    public void logcatFileUpload(Path logcatTempFile, Long exeId, String deviceId) {        log.debug("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.debug("Client response onNext");            }            @Override            public void onError(Throwable t) {                log.debug("Client response onError");            }            @Override            public void onCompleted() {                log.debug("Client response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).logcatFileUpload(responseObserver);        try {            File file = logcatTempFile.toFile();            if (!Files.exists(logcatTempFile)) {                log.error("日志文件不存在, {}", logcatTempFile.toString());                return;            }            String filename = file.getName();            try {                FileInputStream fis = new FileInputStream(file);                BufferedInputStream bInputStream = new BufferedInputStream(fis);                FileChannel fc = fis.getChannel();                long total = fc.size();                int bufferSize = 10 * 1024; // 10k                byte[] buffer = new byte[bufferSize];                int offset = 0;                int size = 0;                while ((size = bInputStream.read(buffer)) > 0) {                    log.debug("offset: {}, size: {}, offset < size: {}");                    ByteString byteString = ByteString.copyFrom(buffer);                    FileUploadRequest req = FileUploadRequest.newBuilder()                            .setName(filename)                            .setData(byteString)                            .setSize(size)                            .setOffset(offset)                            .setExeId(exeId)                            .setDeviceId(deviceId)                            .setTimestamp(TimeUtil.getTimestampLong())                            .setToken(UserInfo.token)                            .build();                    requestObserver.onNext(req);                    offset += size;                }            } catch (IOException e) {                e.printStackTrace();            }        } catch (RuntimeException e) {            requestObserver.onError(e);            throw e;        }        requestObserver.onCompleted();    }    public void executorLogUpload(ExecutorLogInfo logInfo) {        ExecutorLogRequest request = ExecutorLogRequest.newBuilder()                .setDeviceId(logInfo.getDeviceId())                .setExeId(logInfo.getTaskId())                .setToken(UserInfo.token)                .setTimestamp(logInfo.getTime())                .setFlag(logInfo.getFlag())                .setMethodName(logInfo.getMethodName())                .setMethodDesc(logInfo.getMethodDesc())                .setArgs(logInfo.getArgs())                .setOrder(logInfo.getOrder())                .build();        Gvice.taskService(serverChannel).executorLogUpload(request);    }    public void deviceDisconnect(String deviceId) {        DeviceStatusChangeRequest request = DeviceStatusChangeRequest.newBuilder()                .setDeviceId(deviceId)                .setStatus(DeviceStatusChangeRequest.LineStatus.DISCONNECTED)                .build();        Gvice.deviceService(serverChannel).disconnect(request);    }    public void deviceOffline(String deviceId) {        DeviceStatusChangeRequest request = DeviceStatusChangeRequest.newBuilder()                .setDeviceId(deviceId)                .setStatus(DeviceStatusChangeRequest.LineStatus.OFFLINE)                .build();        ListenableFuture<io.rpc.testwa.device.CommonReply> reply = Gvice.deviceService(serverChannel).offline(request);        try {            io.rpc.testwa.device.CommonReply c = reply.get();            log.info(c.getMessage());        } catch (InterruptedException | ExecutionException e) {            log.error("初始化设备失败", e);        }finally {        }    }    public void deviceOnline(String deviceId) {        DeviceStatusChangeRequest request = DeviceStatusChangeRequest.newBuilder()                .setDeviceId(deviceId)                .setStatus(DeviceStatusChangeRequest.LineStatus.ONLINE)                .build();        ListenableFuture<io.rpc.testwa.device.CommonReply> reply = Gvice.deviceService(serverChannel).online(request);        try {            io.rpc.testwa.device.CommonReply c = reply.get();            log.info(c.getMessage());        } catch (InterruptedException | ExecutionException e) {            log.error("离线通知错误", e);        }finally {        }    }    /**     *@Description: 测试的logcat日志上传     *@Param: [output, exeId]     *@Return: void     *@Author: wen     *@Date: 2018/5/16     */    public void logcatUpload(String content, Long taskId, String deviceId) {        LogcatStrRequest request = LogcatStrRequest.newBuilder()                .setContent(content)                .setDeviceId(deviceId)                .setExeId(taskId)                .setTimestamp(TimeUtil.getTimestampLong())                .build();        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).logcatStrUpload(request);        try {            CommonReply c = reply.get();        } catch (InterruptedException | ExecutionException e) {            log.error("logcat日志通知失败", e);        }finally {        }    }    public void saveStep(StepRequest request) {        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).saveStep(request);        try {            CommonReply c = reply.get();        } catch (InterruptedException | ExecutionException e) {            log.error("monkey步骤日志通知失败", e);        }finally {        }    }    /**     *@Description: 保存性能指标数据     *@Param: [cpu, flow, mem, bat]     *@Return: void     *@Author: wen     *@Date: 2018/5/23     */    public void savePreformance(Double cpu, Integer mem, Integer bat, Integer fps, FlowResult flow, Long taskId, String deviceId) {        PerformanceRequest request = PerformanceRequest.newBuilder()                .setCpu(cpu)                .setBat(bat)                .setDeviceId(deviceId)                .setFps(fps)                .setGprsDown(flow.getGprsDown())                .setGprsUp(flow.getGprsUp())                .setWifiDown(flow.getWifiDown())                .setWifiUp(flow.getWifiUp())                .setTaskId(taskId)                .setMem(mem)                .setToken(UserInfo.token)                .setTimestamp(System.currentTimeMillis())                .build();        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).savePerformance(request);        try {            CommonReply c = reply.get();        } catch (InterruptedException | ExecutionException e) {            log.error("monkey性能日志通知失败", e);        }finally {        }    }    public void saveImgDir(String screen, Long exeId, String deviceId) {        try {            Files.list(Paths.get(screen)).forEach( i -> {                imgUpload(exeId, i.toString(), deviceId);            });        } catch (IOException e) {            e.printStackTrace();        }    }}