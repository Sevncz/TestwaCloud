package com.testwa.distest.client.service;import com.alibaba.fastjson.JSON;import com.android.ddmlib.IDevice;import com.github.cosysoft.device.android.AndroidDevice;import com.github.cosysoft.device.shell.ShellCommandException;import com.google.protobuf.ByteString;import com.testwa.core.utils.TimeUtil;import com.testwa.distest.client.ApplicationContextUtil;import com.testwa.distest.client.android.AndroidHelper;import com.testwa.distest.client.component.minicap.Size;import com.testwa.distest.client.control.client.Clients;import com.testwa.distest.client.control.client.RemoteClient;import com.testwa.distest.client.grpc.GrpcClient;import com.testwa.distest.client.grpc.Gvice;import com.testwa.distest.client.model.ExecutorLogInfo;import com.testwa.distest.client.model.UserInfo;import com.testwa.distest.client.component.Constant;import io.grpc.Channel;import io.grpc.stub.StreamObserver;import io.rpc.testwa.device.ConnectedRequest;import io.rpc.testwa.task.*;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import org.springframework.beans.factory.annotation.Value;import org.springframework.core.env.Environment;import org.springframework.scheduling.annotation.Async;import org.springframework.stereotype.Service;import java.io.*;import java.net.URISyntaxException;import java.nio.channels.FileChannel;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.HashMap;import java.util.Map;@Slf4j@Servicepublic class GrpcClientService {    @GrpcClient("local-grpc-server")    private Channel serverChannel;    @Value("${distest.agent.resources}")    private String resourcesPath;    @Async    public void createRemoteClient(IDevice device){        Clients.remove(device.getSerialNumber());        Environment env = ApplicationContextUtil.getApplicationContext().getEnvironment();        String url = env.getProperty("cloud.socket.url");        String wsUrl = String.format("%s?token=%s&type=device&serial=%s&from=BaseClient", url, UserInfo.token, device.getSerialNumber());        try {            RemoteClient remoteClient = new RemoteClient(wsUrl, device.getSerialNumber(), serverChannel, resourcesPath);            Clients.add(device.getSerialNumber(), remoteClient);        } catch (URISyntaxException e) {            log.error("设备连接错误: ", e);        }    }    @Async    public void initDevice(AndroidDevice dev){        log.info("设备 " + dev.getSerialNumber() + "通知服务器初始化");        int time = 5;        int sleep = 500;        while (true) {            // 尝试maxtime的次数之后结束            if(time == 0){                break;            }            // 设备没有同意连接，不做设备信息更新，等待下次检查时更新            try {                String brand = dev.runAdbCommand("shell getprop ro.product.brand");                if(StringUtils.isEmpty(brand) || StringUtils.isEmpty(UserInfo.token)){                    time--;                    try {                        Thread.sleep(sleep);                    } catch (InterruptedException e) {                        e.printStackTrace();                    }                    continue;                }                String cpuabi = dev.runAdbCommand("shell getprop ro.product.cpu.abi");                String sdk = dev.runAdbCommand("shell getprop ro.build.version.sdk");                String host = dev.runAdbCommand("shell getprop ro.build.host");                String model = dev.runAdbCommand("shell getprop ro.product.model");                String version = dev.runAdbCommand("shell getprop ro.build.version.release");                String density = dev.getDevice().getDensity() + "";                String targetPlatform = "";                if (dev.getTargetPlatform() != null) {                    targetPlatform = dev.getTargetPlatform().formatedName();                }                String width = "";                String height = "";//                if (dev.getScreenSize() != null) {//                    width = String.valueOf(dev.getScreenSize().getWidth());//                    height = String.valueOf(dev.getScreenSize().getHeight());//                }                // 获取设备屏幕物理分辨率                String output = dev.runAdbCommand("shell wm size");                if (output != null && !output.isEmpty()) {                    String overrideSizeFlag = "Override";                    if(output.contains(overrideSizeFlag)){                        output = output.split("\n")[0].trim();                    }                    String sizeStr = output.split(":")[1].trim();                    width = sizeStr.split("x")[0].trim();                    height = sizeStr.split("x")[1].trim();                }                ConnectedRequest reuqest = ConnectedRequest.newBuilder()                        .setDeviceId(dev.getSerialNumber())                        .setBrand(brand)                        .setCpuabi(cpuabi)                        .setDensity(density)                        .setHeight(height)                        .setWidth(width)                        .setHost(host)                        .setModel(model)                        .setOsName(targetPlatform)                        .setSdk(sdk)                        .setToken(UserInfo.token)                        .setVersion(version)                        .build();                Gvice.deviceService(serverChannel).connect(reuqest);                break;            }catch (ShellCommandException e){                try {                    Thread.sleep(sleep);                } catch (InterruptedException e1) {                    e.printStackTrace();                }                time--;                continue;            }        }    }    @Async    public void procedureInfoUpload(String urlInfo) {        if(!StringUtils.isEmpty(urlInfo)){            Map<String, Object> payload = JSON.parseObject(urlInfo);            if(payload == null){                payload = new HashMap<>();            }            payload.put("timestamp", TimeUtil.getTimestampLong());            payload.put("createDate", TimeUtil.getTimestamp());            urlInfo = JSON.toJSONString(payload);            ProcedureInfoUploadRequest procedureInfoUploadRequest = ProcedureInfoUploadRequest.newBuilder()                    .setInfoJson(urlInfo)                    .build();            Gvice.taskService(serverChannel).procedureInfoUpload(procedureInfoUploadRequest);            String screenshotPath = (String) payload.get("screenshotPath");            if(StringUtils.isNotEmpty(screenshotPath)){                Long exeId = Long.parseLong((String) payload.get("executionTaskId"));                Path imgPath = Paths.get(Constant.localScreenshotPath, screenshotPath);                log.info("上传截图, {}", imgPath.toString());                imgUpload(exeId, imgPath.toString());            }        }    }    public void notifyServerCurrentTaskExecutorInfo(String deviceId, Long taskId, Long currScriptId, Long currTestcaseId) {        CurrentExeInfoRequest request = CurrentExeInfoRequest.newBuilder()                .setDeviceId(deviceId)                .setExeId(taskId)                .setScriptId(currScriptId)                .setTestcaseId(currTestcaseId)                .setToken(UserInfo.token)                .build();        Gvice.taskService(serverChannel).currExeInfo(request);    }    /**     *@Description: 测试任务失败     *@Param: [exeId, errorMessage]     *@Return: void     *@Author: wen     *@Date: 2018/5/2     */    public void gameover(Long exeId, String deviceId, String errorMessage) {        GameOverRequest request = GameOverRequest.newBuilder()                .setToken(UserInfo.token)                .setExeId(exeId)                .setDeviceId(deviceId)                .setErrorMessage(errorMessage)                .setTimestamp(TimeUtil.getTimestampLong())                .build();        Gvice.taskService(serverChannel).gameover(request);    }    /**     *@Description: 测试任务完成     *@Param: [exeId]     *@Return: void     *@Author: wen     *@Date: 2018/5/2     */    public void missionComplete(Long exeId, String deviceId) {        MissionCompleteRequest request = MissionCompleteRequest.newBuilder()                .setToken(UserInfo.token)                .setExeId(exeId)                .setDeviceId(deviceId)                .setTimestamp(TimeUtil.getTimestampLong())                .build();        Gvice.taskService(serverChannel).missionComplete(request);    }    @Async    public void appiumLogUpload(Long exeId, String deviceId, String appiumLogPath) {        log.debug("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.debug("Client response onNext");            }            @Override            public void onError(Throwable t) {                log.debug("Client response onError");            }            @Override            public void onCompleted() {                log.debug("Client response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).appiumLogUpload(responseObserver);        try {            File file = new File(appiumLogPath);            if (!file.exists()) {                log.error("Appium日志文件不存在, {}", appiumLogPath);                return;            }            String filename = file.getName();            try {                FileInputStream fis = new FileInputStream(file);                BufferedInputStream bInputStream = new BufferedInputStream(fis);                FileChannel fc = fis.getChannel();                long total = fc.size();                int bufferSize = 10 * 1024; // 10k                byte[] buffer = new byte[bufferSize];                int offset = 0;                int size = 0;                while ((size = bInputStream.read(buffer)) > 0) {                    ByteString byteString = ByteString.copyFrom(buffer);                    FileUploadRequest req = FileUploadRequest.newBuilder()                            .setName(filename)                            .setData(byteString)                            .setSize(size)                            .setOffset(offset)                            .setExeId(exeId)                            .setDeviceId(deviceId)                            .setTimestamp(TimeUtil.getTimestampLong())                            .setToken(UserInfo.token)                            .build();                    requestObserver.onNext(req);                    offset += size;                }            } catch (IOException e) {                e.printStackTrace();            }        } catch (RuntimeException e) {            requestObserver.onError(e);            throw e;        }        requestObserver.onCompleted();    }    @Async    public void imgUpload(Long exeId, String imgPath) {        log.debug("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.debug("Client response onNext");            }            @Override            public void onError(Throwable t) {                log.debug("Client response onError");            }            @Override            public void onCompleted() {                log.debug("Client response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).imgUpload(responseObserver);        try {            File file = new File(imgPath);            if (!file.exists()) {                log.error("截图文件不存在, {}", imgPath);                return;            }            String filename = file.getName();            try {                FileInputStream fis = new FileInputStream(file);                BufferedInputStream bInputStream = new BufferedInputStream(fis);                FileChannel fc = fis.getChannel();                long total = fc.size();                int bufferSize = 10 * 1024; // 10k                byte[] buffer = new byte[bufferSize];                int offset = 0;                int size = 0;                while ((size = bInputStream.read(buffer)) > 0) {                    log.debug("offset: {}, size: {}, offset < size: {}");                    ByteString byteString = ByteString.copyFrom(buffer);                    FileUploadRequest req = FileUploadRequest.newBuilder()                            .setName(filename)                            .setData(byteString)                            .setSize(size)                            .setOffset(offset)                            .setExeId(exeId)                            .setTimestamp(TimeUtil.getTimestampLong())                            .setToken(UserInfo.token)                            .build();                    requestObserver.onNext(req);                    offset += size;                }//                Files.delete(Paths.get(imgPath));            } catch (IOException e) {                e.printStackTrace();            }        } catch (RuntimeException e) {            requestObserver.onError(e);            throw e;        }        requestObserver.onCompleted();    }    public void executorLogUpload(ExecutorLogInfo logInfo) {        ExecutorLogRequest request = ExecutorLogRequest.newBuilder()                .setDeviceId(logInfo.getDeviceId())                .setExeId(logInfo.getTaskId())                .setToken(UserInfo.token)                .setTimestamp(logInfo.getTime())                .setFlag(logInfo.getFlag())                .setMethodName(logInfo.getMethodName())                .setAction(logInfo.getAction())                .setArgs(logInfo.getArgs())                .setOrder(logInfo.getOrder())                .build();        Gvice.taskService(serverChannel).executorLogUpload(request);    }}