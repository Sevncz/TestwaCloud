package com.testwa.distest.client.service;import com.alibaba.fastjson.JSON;import com.github.cosysoft.device.android.AndroidDevice;import com.google.common.util.concurrent.ListenableFuture;import com.google.protobuf.ByteString;import com.testwa.core.utils.TimeUtil;import com.testwa.distest.client.control.client.Clients;import com.testwa.distest.client.control.client.DeviceClient;import com.testwa.distest.client.control.client.DeviceClientCache;import com.testwa.distest.client.control.client.RemoteClient;import com.testwa.distest.client.grpc.GrpcClient;import com.testwa.distest.client.grpc.Gvice;import com.testwa.distest.client.model.ExecutorLogInfo;import com.testwa.distest.client.model.IOSDevice;import com.testwa.distest.client.model.UserInfo;import com.testwa.distest.client.component.Constant;import io.grpc.Channel;import io.grpc.stub.StreamObserver;import io.rpc.testwa.device.DeviceStatusChangeRequest;import io.rpc.testwa.task.*;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import org.springframework.beans.factory.annotation.Value;import org.springframework.scheduling.annotation.Async;import org.springframework.stereotype.Service;import java.io.*;import java.net.URISyntaxException;import java.nio.channels.FileChannel;import java.nio.file.Path;import java.nio.file.Paths;import java.util.HashMap;import java.util.Map;import java.util.concurrent.ExecutionException;@Slf4j@Servicepublic class GrpcClientService {    @GrpcClient("local-grpc-server")    private Channel serverChannel;    @Value("${distest.agent.resources}")    private String resourcesPath;    @Value("${cloud.socket.url}")    private String socketUrl;    @Value("${username}")    private String username;    /**     *@Description: 检查缓存中是否存在remoteClient，如果存在，则不创建     *@Param: [device]     *@Return: void     *@Author: wen     *@Date: 2018/5/4     */    private synchronized void createRemoteClient(AndroidDevice ad){        if(Clients.get(ad.getSerialNumber()) == null){            String wsUrl = String.format("%s?token=%s&type=device&serial=%s&from=BaseClient", socketUrl, UserInfo.token, ad.getSerialNumber());            try {                RemoteClient remoteClient = new RemoteClient(wsUrl, ad.getSerialNumber(), serverChannel, resourcesPath);                Clients.add(ad.getSerialNumber(), remoteClient);            } catch (URISyntaxException e) {                log.error("设备连接错误: ", e);            }        }    }    public void initDevice(AndroidDevice dev){        DeviceClient dc = createDeviceClient(dev);        dc.registerToServer();    }    public DeviceClient createDeviceClient(AndroidDevice dev){        // 注册服务        DeviceClient dc = DeviceClientCache.get(dev.getSerialNumber());        if(dc == null){            dc = new DeviceClient(dev.getSerialNumber(), serverChannel, resourcesPath, username);            DeviceClientCache.add(dev.getSerialNumber(), dc);        }        return dc;    }    @Async    public void initDevice(IOSDevice d) {//        log.debug("iOS 设备 " + d.getDeviceId() + " 通知服务器初始化");    }    @Async    public void procedureInfoUpload(String urlInfo) {        if(!StringUtils.isEmpty(urlInfo)){            Map<String, Object> payload = JSON.parseObject(urlInfo);            if(payload == null){                payload = new HashMap<>();            }            payload.put("timestamp", TimeUtil.getTimestampLong());            payload.put("createDate", TimeUtil.getTimestamp());            urlInfo = JSON.toJSONString(payload);            ProcedureInfoUploadRequest procedureInfoUploadRequest = ProcedureInfoUploadRequest.newBuilder()                    .setInfoJson(urlInfo)                    .build();            Gvice.taskService(serverChannel).procedureInfoUpload(procedureInfoUploadRequest);            String screenshotPath = (String) payload.get("screenshotPath");            if(StringUtils.isNotEmpty(screenshotPath)){                Long exeId = Long.parseLong((String) payload.get("executionTaskId"));                Path imgPath = Paths.get(Constant.localScreenshotPath, screenshotPath);                log.info("上传截图, {}", imgPath.toString());                imgUpload(exeId, imgPath.toString());            }        }    }    public void notifyServerCurrentTaskExecutorInfo(String deviceId, Long taskId, Long currScriptId, Long currTestcaseId) {        CurrentExeInfoRequest request = CurrentExeInfoRequest.newBuilder()                .setDeviceId(deviceId)                .setExeId(taskId)                .setScriptId(currScriptId)                .setTestcaseId(currTestcaseId)                .setToken(UserInfo.token)                .build();        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).currExeInfo(request);        try {            CommonReply c = reply.get();            log.info(c.getMessage());        } catch (InterruptedException | ExecutionException e) {            log.error("任务执行情况通知失败", e);        }finally {        }    }    /**     *@Description: 测试任务失败     *@Param: [exeId, errorMessage]     *@Return: void     *@Author: wen     *@Date: 2018/5/2     */    public void gameover(Long exeId, String deviceId, String errorMessage) {        GameOverRequest request = GameOverRequest.newBuilder()                .setToken(UserInfo.token)                .setExeId(exeId)                .setDeviceId(deviceId)                .setErrorMessage(errorMessage)                .setTimestamp(TimeUtil.getTimestampLong())                .build();        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).gameover(request);        try {            CommonReply c = reply.get();            log.info(c.getMessage());        } catch (InterruptedException | ExecutionException e) {            log.error("任务执行失败通知失败", e);        }finally {        }    }    /**     *@Description: 测试任务完成     *@Param: [exeId]     *@Return: void     *@Author: wen     *@Date: 2018/5/2     */    public void missionComplete(Long exeId, String deviceId) {        MissionCompleteRequest request = MissionCompleteRequest.newBuilder()                .setToken(UserInfo.token)                .setExeId(exeId)                .setDeviceId(deviceId)                .setTimestamp(TimeUtil.getTimestampLong())                .build();        ListenableFuture<CommonReply> reply = Gvice.taskService(serverChannel).missionComplete(request);        try {            CommonReply c = reply.get();            log.info(c.getMessage());        } catch (InterruptedException | ExecutionException e) {            log.error("任务执行成功通知失败", e);        }finally {        }    }    @Async    public void appiumLogUpload(Long exeId, String deviceId, String appiumLogPath) {        log.debug("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.debug("Client response onNext");            }            @Override            public void onError(Throwable t) {                log.debug("Client response onError");            }            @Override            public void onCompleted() {                log.debug("Client response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).appiumLogUpload(responseObserver);        try {            File file = new File(appiumLogPath);            if (!file.exists()) {                log.error("Appium日志文件不存在, {}", appiumLogPath);                return;            }            String filename = file.getName();            try {                FileInputStream fis = new FileInputStream(file);                BufferedInputStream bInputStream = new BufferedInputStream(fis);                FileChannel fc = fis.getChannel();                long total = fc.size();                int bufferSize = 10 * 1024; // 10k                byte[] buffer = new byte[bufferSize];                int offset = 0;                int size = 0;                while ((size = bInputStream.read(buffer)) > 0) {                    ByteString byteString = ByteString.copyFrom(buffer);                    FileUploadRequest req = FileUploadRequest.newBuilder()                            .setName(filename)                            .setData(byteString)                            .setSize(size)                            .setOffset(offset)                            .setExeId(exeId)                            .setDeviceId(deviceId)                            .setTimestamp(TimeUtil.getTimestampLong())                            .setToken(UserInfo.token)                            .build();                    requestObserver.onNext(req);                    offset += size;                }            } catch (IOException e) {                e.printStackTrace();            }        } catch (RuntimeException e) {            requestObserver.onError(e);            throw e;        }        requestObserver.onCompleted();    }    @Async    public void imgUpload(Long exeId, String imgPath) {        log.debug("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.debug("Client response onNext");            }            @Override            public void onError(Throwable t) {                log.debug("Client response onError");            }            @Override            public void onCompleted() {                log.debug("Client response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).imgUpload(responseObserver);        try {            File file = new File(imgPath);            if (!file.exists()) {                log.error("截图文件不存在, {}", imgPath);                return;            }            String filename = file.getName();            try {                FileInputStream fis = new FileInputStream(file);                BufferedInputStream bInputStream = new BufferedInputStream(fis);                FileChannel fc = fis.getChannel();                long total = fc.size();                int bufferSize = 10 * 1024; // 10k                byte[] buffer = new byte[bufferSize];                int offset = 0;                int size = 0;                while ((size = bInputStream.read(buffer)) > 0) {                    log.debug("offset: {}, size: {}, offset < size: {}");                    ByteString byteString = ByteString.copyFrom(buffer);                    FileUploadRequest req = FileUploadRequest.newBuilder()                            .setName(filename)                            .setData(byteString)                            .setSize(size)                            .setOffset(offset)                            .setExeId(exeId)                            .setTimestamp(TimeUtil.getTimestampLong())                            .setToken(UserInfo.token)                            .build();                    requestObserver.onNext(req);                    offset += size;                }//                Files.delete(Paths.get(imgPath));            } catch (IOException e) {                e.printStackTrace();            }        } catch (RuntimeException e) {            requestObserver.onError(e);            throw e;        }        requestObserver.onCompleted();    }    public void executorLogUpload(ExecutorLogInfo logInfo) {        ExecutorLogRequest request = ExecutorLogRequest.newBuilder()                .setDeviceId(logInfo.getDeviceId())                .setExeId(logInfo.getTaskId())                .setToken(UserInfo.token)                .setTimestamp(logInfo.getTime())                .setFlag(logInfo.getFlag())                .setMethodName(logInfo.getMethodName())                .setAction(logInfo.getAction())                .setArgs(logInfo.getArgs())                .setOrder(logInfo.getOrder())                .build();        Gvice.taskService(serverChannel).executorLogUpload(request);    }    public void deviceDisconnect(String deviceId) {        DeviceStatusChangeRequest request = DeviceStatusChangeRequest.newBuilder()                .setDeviceId(deviceId)                .setStatus(DeviceStatusChangeRequest.LineStatus.DISCONNECTED)                .build();        Gvice.deviceService(serverChannel).disconnect(request);    }    public void deviceOffline(String deviceId) {        DeviceStatusChangeRequest request = DeviceStatusChangeRequest.newBuilder()                .setDeviceId(deviceId)                .setStatus(DeviceStatusChangeRequest.LineStatus.OFFLINE)                .build();        ListenableFuture<io.rpc.testwa.device.CommonReply> reply = Gvice.deviceService(serverChannel).offline(request);        try {            io.rpc.testwa.device.CommonReply c = reply.get();            log.info(c.getMessage());        } catch (InterruptedException | ExecutionException e) {            log.error("初始化设备失败", e);        }finally {        }    }    public void deviceOnline(String deviceId) {        DeviceStatusChangeRequest request = DeviceStatusChangeRequest.newBuilder()                .setDeviceId(deviceId)                .setStatus(DeviceStatusChangeRequest.LineStatus.ONLINE)                .build();        ListenableFuture<io.rpc.testwa.device.CommonReply> reply = Gvice.deviceService(serverChannel).online(request);        try {            io.rpc.testwa.device.CommonReply c = reply.get();            log.info(c.getMessage());        } catch (InterruptedException | ExecutionException e) {            log.error("离线通知错误", e);        }finally {        }    }}