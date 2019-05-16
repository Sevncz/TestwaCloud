package com.testwa.distest.client.service;import com.alibaba.fastjson.JSON;import com.github.cosysoft.device.android.AndroidDevice;import com.google.common.util.concurrent.ListenableFuture;import com.google.protobuf.ByteString;import com.testwa.core.utils.TimeUtil;import com.testwa.distest.client.DeviceClient;import com.testwa.distest.client.DeviceClientManager;import com.testwa.distest.client.android.AndroidHelper;import com.testwa.distest.client.android.DeviceValidator;import com.testwa.distest.client.component.FlowResult;import com.testwa.distest.client.exception.DeviceNotReadyException;import com.testwa.distest.client.ios.IOSDeviceUtil;import com.testwa.distest.client.model.ExecutorLogInfo;import com.testwa.distest.client.model.UserInfo;import com.testwa.distest.client.component.Constant;import io.grpc.Channel;import io.grpc.stub.StreamObserver;import io.rpc.testwa.device.DeviceType;import io.rpc.testwa.task.*;import lombok.extern.slf4j.Slf4j;import net.devh.springboot.autoconfigure.grpc.client.GrpcClient;import org.apache.commons.lang3.StringUtils;import org.springframework.beans.factory.annotation.Value;import org.springframework.scheduling.annotation.Async;import org.springframework.stereotype.Component;import java.io.*;import java.nio.channels.FileChannel;import java.nio.file.FileVisitOption;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.HashMap;import java.util.Map;import java.util.concurrent.ExecutionException;@Slf4j@Componentpublic class GrpcClientService {    @Value("${distest.agent.resources}")    private String resourcesPath;    @Value("${username}")    private String username;    @GrpcClient("grpc-server")    private Channel serverChannel;    private static final int BUFFER_SIZE = 1024*1024;    @Async    public void initAndroidDevice(String deviceId) throws DeviceNotReadyException {        if(StringUtils.isBlank(deviceId)){            log.error("deviceId is null, init device client error ");            return;        }        if(!DeviceValidator.isLocalDevice(deviceId)) {            return;        }        // 注册服务        DeviceClient dc = DeviceClientManager.get(deviceId);        if(dc != null){            return;        }else{            if(StringUtils.isBlank(UserInfo.token)) {                log.error("用户未登录");                return;            }            AndroidDevice ad = AndroidHelper.getInstance().getAndroidDevice(deviceId);            if(ad != null && ad.getDevice().isOnline()){                dc = new DeviceClient(deviceId, serverChannel, UserInfo.token, DeviceType.ANDROID);                DeviceClientManager.add(deviceId, dc);                dc.initClient();                dc.registerToServer();            }else{                log.debug("设备 {} 处于离线状态", deviceId);            }        }    }    public void destoryDeviceClient(String deviceId) {        DeviceClientManager.remove(deviceId);    }    @Async    public void initIOSDevice(String udid) throws DeviceNotReadyException {        if(StringUtils.isBlank(udid)){            return;        }        // 注册服务        DeviceClient dc = DeviceClientManager.get(udid);        if(dc != null){            return;        }else{            if(StringUtils.isBlank(UserInfo.token)) {                return;            }            if(IOSDeviceUtil.isOnline(udid)){                dc = new DeviceClient(udid, serverChannel, UserInfo.token, DeviceType.IOS);                DeviceClientManager.add(udid, dc);                dc.initClient();                dc.registerToServer();            }else{                log.info("设备 {} 处于离线状态", udid);            }        }    }    @Async    public void procedureInfoUpload(String urlInfo) {        if(!StringUtils.isEmpty(urlInfo)){            Map<String, Object> payload = JSON.parseObject(urlInfo);            if(payload == null){                payload = new HashMap<>();            }            payload.put("timestamp", TimeUtil.getTimestampLong());            payload.put("createDate", TimeUtil.getTimestamp());            urlInfo = JSON.toJSONString(payload);            AppiumRunningLogUploadRequest procedureInfoUploadRequest = AppiumRunningLogUploadRequest.newBuilder()                    .setInfoJson(urlInfo)                    .build();            Gvice.taskService(serverChannel).appiumRunningLogUpload(procedureInfoUploadRequest);            String screenshotPath = (String) payload.get("screenshotPath");            if(StringUtils.isNotEmpty(screenshotPath)){                Long exeId = Long.parseLong((String) payload.get("executionTaskId"));                String deviceId = (String) payload.get("deviceId");                Path imgPath = Paths.get(Constant.localScreenshotPath, screenshotPath);                log.info("上传截图, {}", imgPath.toString());                imgUpload(exeId, imgPath.toString(), deviceId);            }        }    }    /**     *@Description: 测试任务失败     *@Param: [taskCode, errorMessage]     *@Return: void     *@Author: wen     *@Date: 2018/5/2     */    public void gameover(Long exeId, String deviceId, String errorMessage) {        if(StringUtils.isBlank(errorMessage)) {            errorMessage = "没有错误信息";        }        GameOverRequest request = GameOverRequest.newBuilder()                .setToken(UserInfo.token)                .setTaskCode(exeId)                .setDeviceId(deviceId)                .setErrorMessage(errorMessage)                .setTimestamp(TimeUtil.getTimestampLong())                .build();        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).gameover(request);        try {            CommonReply c = reply.get();            log.info("gameover return" + c.getMessage());        } catch (InterruptedException | ExecutionException e) {            log.error("任务执行失败通知失败", e);        }finally {        }    }    /**     *@Description: 测试任务完成     *@Param: [taskCode]     *@Return: void     *@Author: wen     *@Date: 2018/5/2     */    public void missionComplete(Long exeId, String deviceId) {        MissionCompleteRequest request = MissionCompleteRequest.newBuilder()                .setToken(UserInfo.token)                .setTaskCode(exeId)                .setDeviceId(deviceId)                .setTimestamp(TimeUtil.getTimestampLong())                .build();        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).missionComplete(request);        try {            CommonReply c = reply.get();            log.info("missionComplete return" + c.getMessage());        } catch (InterruptedException | ExecutionException e) {            log.error("任务执行成功通知失败", e);        }finally {        }    }    @Async    public void appiumLogUpload(Long exeId, String deviceId, String appiumLogPath) {        log.debug("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.debug("Client response onNext");            }            @Override            public void onError(Throwable t) {                log.debug("Client response onError");            }            @Override            public void onCompleted() {                log.debug("Client response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).appiumLogFileUpload(responseObserver);        try {            File file = new File(appiumLogPath);            if (!file.exists()) {                log.error("Appium日志文件不存在, {}", appiumLogPath);                return;            }            String filename = file.getName();            try {                FileInputStream fis = new FileInputStream(file);                BufferedInputStream bInputStream = new BufferedInputStream(fis);                FileChannel fc = fis.getChannel();                long total = fc.size();                int bufferSize = 100 * 1024;                byte[] buffer = new byte[bufferSize];                int offset = 0;                int size = 0;                while ((size = bInputStream.read(buffer)) > 0) {                    ByteString byteString = ByteString.copyFrom(buffer);                    FileUploadRequest req = FileUploadRequest.newBuilder()                            .setName(filename)                            .setData(byteString)                            .setSize(size)                            .setOffset(offset)                            .setTaskCode(exeId)                            .setDeviceId(deviceId)                            .setTimestamp(TimeUtil.getTimestampLong())                            .setToken(UserInfo.token)                            .build();                    requestObserver.onNext(req);                    offset += size;                }            } catch (IOException e) {                e.printStackTrace();            }        } catch (RuntimeException e) {            requestObserver.onError(e);            throw e;        }        requestObserver.onCompleted();    }    public void imgUpload(Long exeId, String imgFile, String deviceId) {        log.debug("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.debug("Client img response onNext");            }            @Override            public void onError(Throwable t) {                log.debug("Client img response onError");            }            @Override            public void onCompleted() {                log.debug("Client img response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).imgFileUpload(responseObserver);        Path imgPath = Paths.get(imgFile);        try {            File file = imgPath.toFile();            if (!file.exists()) {                log.error("截图文件不存在, {}", imgPath);                return;            }            log.debug("路径：{}  文件大小: {}", imgPath.toString(), Files.size(imgPath));            String filename = file.getName();            byte[] contentBytes = Files.readAllBytes(imgPath);            ByteString byteString = ByteString.copyFrom(contentBytes);            FileUploadRequest req = FileUploadRequest.newBuilder()                    .setName(filename)                    .setData(byteString)                    .setSize(BUFFER_SIZE)                    .setOffset(0)                    .setTaskCode(exeId)                    .setDeviceId(deviceId)                    .setTimestamp(TimeUtil.getTimestampLong())                    .setToken(UserInfo.token)                    .build();            requestObserver.onNext(req);        } catch (RuntimeException | IOException e) {            requestObserver.onError(e);            log.error("上传文件{} 失败", imgFile, e);        } finally {            requestObserver.onCompleted();        }    }    public void logcatFileUpload(Path logcatTempFile, Long taskCode, String deviceId) {        log.debug("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.debug("Client response onNext");            }            @Override            public void onError(Throwable t) {                log.debug("Client response onError");            }            @Override            public void onCompleted() {                log.debug("Client response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).logcatFileUpload(responseObserver);        try {            File file = logcatTempFile.toFile();            if (!Files.exists(logcatTempFile)) {                log.error("日志文件不存在, {}", logcatTempFile.toString());                return;            }            String filename = file.getName();            try {                FileInputStream fis = new FileInputStream(file);                BufferedInputStream bInputStream = new BufferedInputStream(fis);                FileChannel fc = fis.getChannel();                long total = fc.size();                int bufferSize = 10 * 1024; // 10k                byte[] buffer = new byte[bufferSize];                int offset = 0;                int size = 0;                while ((size = bInputStream.read(buffer)) > 0) {                    log.debug("offset: {}, size: {}, offset < size: {}");                    ByteString byteString = ByteString.copyFrom(buffer);                    FileUploadRequest req = FileUploadRequest.newBuilder()                            .setName(filename)                            .setData(byteString)                            .setSize(size)                            .setOffset(offset)                            .setTaskCode(taskCode)                            .setDeviceId(deviceId)                            .setTimestamp(TimeUtil.getTimestampLong())                            .setToken(UserInfo.token)                            .build();                    requestObserver.onNext(req);                    offset += size;                }            } catch (IOException e) {                e.printStackTrace();            }        } catch (RuntimeException e) {            requestObserver.onError(e);            throw e;        }        requestObserver.onCompleted();    }    public void fileUpload(Path localFile, String dir, FileUploadRequest.Type prefix, Long taskCode, String deviceId) {        log.debug("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.debug("Client file response onNext");            }            @Override            public void onError(Throwable t) {                log.debug("Client file response onError");            }            @Override            public void onCompleted() {                log.debug("Client file response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).fileUpload(responseObserver);        try {            if (!Files.exists(localFile)) {                log.error("文件不存在, {}", localFile.toString());                return;            }            log.debug("路径：{}  文件大小: {}", localFile.toString(), Files.size(localFile));            String filename = localFile.toString().replace(dir + File.separator, "").replace(File.separator, "/");            byte[] contentBytes = Files.readAllBytes(localFile);            ByteString byteString = ByteString.copyFrom(contentBytes);            FileUploadRequest req = FileUploadRequest.newBuilder()                    .setName(filename)                    .setType(prefix)                    .setData(byteString)                    .setSize((int) Files.size(localFile))                    .setOffset(0)                    .setTaskCode(taskCode)                    .setDeviceId(deviceId)                    .setTimestamp(TimeUtil.getTimestampLong())                    .setToken(UserInfo.token)                    .build();            requestObserver.onNext(req);        } catch (RuntimeException | IOException e) {            requestObserver.onError(e);            log.error("上传文件{} 失败", localFile, e);        } finally {            requestObserver.onCompleted();        }    }    public void largeFileUpload(Path localFile, String dir, FileUploadRequest.Type prefix, Long taskCode, String deviceId) {        log.info("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.info("Client file response onNext");            }            @Override            public void onError(Throwable t) {                log.debug("Client file response onError");            }            @Override            public void onCompleted() {                log.debug("Client file response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).fileUpload(responseObserver);        try {            if (!Files.exists(localFile)) {                log.error("文件不存在, {}", localFile.toString());                return;            }            log.debug("路径：{}  文件大小: {}", localFile.toString(), Files.size(localFile));            String filename = localFile.toString().replace(dir + File.separator, "").replace(File.separator, "/");            long bytesSize = Files.size(localFile);            int kbSize = (int) (bytesSize / 1024);            try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(localFile.toFile()))) {                byte[] bbuf = new byte[BUFFER_SIZE];                int offset = 0;                int len;                while ((len = in.read(bbuf, 0, BUFFER_SIZE)) != -1) {                    ByteString byteString = ByteString.copyFrom(bbuf, 0, len);                    FileUploadRequest req = FileUploadRequest.newBuilder()                            .setName(filename)                            .setType(prefix)                            .setData(byteString)                            .setSize(kbSize)                            .setOffset(offset)                            .setTaskCode(taskCode)                            .setDeviceId(deviceId)                            .setTimestamp(TimeUtil.getTimestampLong())                            .setToken(UserInfo.token)                            .build();                    requestObserver.onNext(req);                    offset += len;                }            }        } catch (RuntimeException | IOException e) {            requestObserver.onError(e);            log.error("上传文件{} 失败", localFile, e);        } finally {            requestObserver.onCompleted();        }    }    public void executorLogUpload(ExecutorLogInfo logInfo) {        ExecutorLogRequest request = ExecutorLogRequest.newBuilder()                .setDeviceId(logInfo.getDeviceId())                .setTaskCode(logInfo.getTaskId())                .setToken(UserInfo.token)                .setTimestamp(logInfo.getTime())                .setFlag(logInfo.getFlag())                .setMethodName(logInfo.getMethodName())                .setMethodDesc(logInfo.getMethodDesc())                .setArgs(logInfo.getArgs())                .setOrder(logInfo.getOrder())                .build();        Gvice.taskService(serverChannel).executorLogUpload(request);    }    /**     *@Description: 测试的logcat日志上传     *@Param: [output, taskCode]     *@Return: void     *@Author: wen     *@Date: 2018/5/16     */    public void logcatUpload(String content, Long taskId, String deviceId) {        LogcatStrRequest request = LogcatStrRequest.newBuilder()                .setContent(content)                .setDeviceId(deviceId)                .setTaskCode(taskId)                .setTimestamp(TimeUtil.getTimestampLong())                .build();        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).logcatStrUpload(request);        try {            CommonReply c = reply.get();        } catch (InterruptedException | ExecutionException e) {            log.error("logcat日志通知失败", e);        }finally {        }    }    public void saveStep(StepRequest request) {        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).saveStep(request);        try {            CommonReply c = reply.get();        } catch (InterruptedException | ExecutionException e) {            log.error("monkey步骤日志通知失败", e);        }finally {        }    }    /**     *@Description: 保存性能指标数据     *@Param: [cpu, flow, ram, bat]     *@Return: void     *@Author: wen     *@Date: 2018/5/23     */    public void savePreformance(Double cpu, Integer mem, Integer bat, Integer fps, FlowResult flow, Long taskId, String deviceId) {        PerformanceRequest request = PerformanceRequest.newBuilder()                .setCpu(cpu)                .setBat(bat)                .setDeviceId(deviceId)                .setFps(fps)                .setGprsDown(flow.getGprsDown())                .setGprsUp(flow.getGprsUp())                .setWifiDown(flow.getWifiDown())                .setWifiUp(flow.getWifiUp())                .setTaskCode(taskId)                .setMem(mem)                .setToken(UserInfo.token)                .setTimestamp(System.currentTimeMillis())                .build();        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).savePerformance(request);        try {            CommonReply c = reply.get();        } catch (InterruptedException | ExecutionException e) {            log.error("monkey性能日志通知失败", e);        }finally {        }    }    public void saveImgDir(String screen, Long exeId, String deviceId) {        try {            Files.walk(Paths.get(screen), 2, FileVisitOption.values()).forEach(i -> {                if(i.toFile().isFile()){                    imgUpload(exeId, i.toString(), deviceId);                }            });        } catch (IOException e) {            e.printStackTrace();        }    }    public String getTaskConfig(Long taskCode) {        TaskCodeRequest request = TaskCodeRequest.newBuilder()                .setTaskCode(taskCode)                .build();        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).taskConfig(request);        try {            CommonReply c = reply.get();            if(c.getStatus() != 0) {                return c.getMessage();            }        } catch (InterruptedException | ExecutionException e) {            log.error("monkey性能日志通知失败", e);        }finally {        }        return null;    }}