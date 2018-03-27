package com.testwa.distest.client.service;import com.alibaba.fastjson.JSON;import com.android.ddmlib.IDevice;import com.github.cosysoft.device.android.AndroidDevice;import com.github.cosysoft.device.shell.ShellCommandException;import com.google.protobuf.ByteString;import com.testwa.core.utils.TimeUtil;import com.testwa.distest.client.ApplicationContextUtil;import com.testwa.distest.client.control.client.Clients;import com.testwa.distest.client.control.client.RemoteClient;import com.testwa.distest.client.grpc.GrpcClient;import com.testwa.distest.client.grpc.Gvice;import com.testwa.distest.client.model.ExecutorLogInfo;import com.testwa.distest.client.model.UserInfo;import com.testwa.distest.client.component.Constant;import io.grpc.Channel;import io.grpc.stub.StreamObserver;import io.rpc.testwa.device.ConnectedRequest;import io.rpc.testwa.task.*;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import org.springframework.beans.factory.annotation.Value;import org.springframework.core.env.Environment;import org.springframework.scheduling.annotation.Async;import org.springframework.stereotype.Service;import java.io.*;import java.net.URISyntaxException;import java.nio.channels.FileChannel;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.HashMap;import java.util.Map;@Slf4j@Servicepublic class GrpcClientService {    @GrpcClient("local-grpc-server")    private Channel serverChannel;    @Value("${distest.agent.resources}")    private String resourcesPath;    @Async    public void createRemoteClient(IDevice device){        Clients.remove(device.getSerialNumber());        Environment env = ApplicationContextUtil.getApplicationContext().getEnvironment();        String url = env.getProperty("agent.socket.url");        String wsUrl = String.format("%s?token=%s&type=device&serial=%s&from=BaseClient", url, UserInfo.token, device.getSerialNumber());        try {            RemoteClient remoteClient = new RemoteClient(wsUrl, "", device.getSerialNumber(), serverChannel, resourcesPath);            Clients.add(device.getSerialNumber(), remoteClient);        } catch (IOException | URISyntaxException e) {            e.printStackTrace();        }    }    @Async    public void initDevice(AndroidDevice dev){        log.info("device【" + dev.getSerialNumber() + "】init sending......");        int time = 5;        int sleep = 500;        while (true) {            // 尝试maxtime的次数之后结束            if(time == 0){                break;            }            // 设备没有同意连接，不做设备信息更新，等待下次检查时更新            try {                String brand = dev.runAdbCommand("shell getprop ro.product.brand");                if(StringUtils.isEmpty(brand) || StringUtils.isEmpty(UserInfo.token)){                    time--;                    try {                        Thread.sleep(sleep);                    } catch (InterruptedException e) {                        e.printStackTrace();                    }                    continue;                }                String cpuabi = dev.runAdbCommand("shell getprop ro.product.cpu.abi");                String sdk = dev.runAdbCommand("shell getprop ro.build.version.sdk");                String host = dev.runAdbCommand("shell getprop ro.build.host");                String model = dev.runAdbCommand("shell getprop ro.product.dto");                String version = dev.runAdbCommand("shell getprop ro.build.version.release");                String density = dev.getDevice().getDensity() + "";                String targetPlatform = "";                if (dev.getTargetPlatform() != null) {                    targetPlatform = dev.getTargetPlatform().formatedName();                }                String width = "";                String height = "";                if (dev.getScreenSize() != null) {                    width = String.valueOf(dev.getScreenSize().getWidth());                    height = String.valueOf(dev.getScreenSize().getHeight());                }                ConnectedRequest reuqest = ConnectedRequest.newBuilder()                        .setDeviceId(dev.getSerialNumber())                        .setBrand(brand)                        .setCpuabi(cpuabi)                        .setDensity(density)                        .setHeight(height)                        .setWidth(width)                        .setHost(host)                        .setModel(model)                        .setOsName(targetPlatform)                        .setSdk(sdk)                        .setToken(UserInfo.token)                        .setVersion(version)                        .build();                Gvice.deviceService(serverChannel).connect(reuqest);                break;            }catch (ShellCommandException e){                try {                    Thread.sleep(sleep);                } catch (InterruptedException e1) {                    e.printStackTrace();                }                time--;                continue;            }        }    }    @Async    public void procedureInfoUpload(String urlInfo) {        if(!StringUtils.isEmpty(urlInfo)){            Map<String, Object> payload = JSON.parseObject(urlInfo);            if(payload == null){                payload = new HashMap<>();            }            payload.put("timestamp", TimeUtil.getTimestampLong());            payload.put("createDate", TimeUtil.getTimestamp());            urlInfo = JSON.toJSONString(payload);            ProcedureInfoUploadRequest procedureInfoUploadRequest = ProcedureInfoUploadRequest.newBuilder()                    .setInfoJson(urlInfo)                    .build();            Gvice.taskService(serverChannel).procedureInfoUpload(procedureInfoUploadRequest);            String screenshotPath = (String) payload.get("screenshotPath");            if(StringUtils.isNotEmpty(screenshotPath)){                Long exeId = Long.parseLong((String) payload.get("executionTaskId"));                Path imgPath = Paths.get(Constant.localScreenshotPath, screenshotPath);                log.info("upload screen img, {}", imgPath.toString());                imgUpload(exeId, imgPath.toString());            }        }    }    public void notifyServerCurrentTaskExecutorInfo(String deviceId, Long taskId, Long currScriptId, Long currTestcaseId) {        CurrentExeInfoRequest request = CurrentExeInfoRequest.newBuilder()                .setDeviceId(deviceId)                .setExeId(taskId)                .setScriptId(currScriptId)                .setTestcaseId(currTestcaseId)                .setToken(UserInfo.token)                .build();        Gvice.taskService(serverChannel).currExeInfo(request);    }    public void gameover(Long exeId) {        // 测试结束，通知服务器，任务已结束        TaskOverRequest taskOverRequest = TaskOverRequest.newBuilder()                .setToken(UserInfo.token)                .setExeId(exeId)                .setTimestamp(TimeUtil.getTimestampLong())                .build();        Gvice.taskService(serverChannel).gameover(taskOverRequest);    }    @Async    public void logcatUpload(Long exeId, String deviceId, String logcatPath) {        log.info("tid: " +  Thread.currentThread().getId() + ", Will try to logcatUpload");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.info("Client response onNext");            }            @Override            public void onError(Throwable t) {                log.info("Client response onError");            }            @Override            public void onCompleted() {                log.info("Client response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).logcatUpload(responseObserver);        try {            File file = new File(logcatPath);            if (!file.exists()) {                log.info("logcat file does not exist");                return;            }            try {                BufferedInputStream bInputStream = new BufferedInputStream(new FileInputStream(file));                int bufferSize = 512 * 1024; // 512k                byte[] buffer = new byte[bufferSize];                int tmp = 0;                int size = 0;                while ((tmp = bInputStream.read(buffer)) > 0) {                    size += tmp;                    ByteString byteString = ByteString.copyFrom(buffer);                    FileUploadRequest req = FileUploadRequest.newBuilder()                            .setName(logcatPath)                            .setData(byteString)                            .setOffset(size)                            .setExeId(exeId)                            .setTimestamp(TimeUtil.getTimestampLong())                            .setToken(UserInfo.token)                            .build();                    requestObserver.onNext(req);                }            } catch (FileNotFoundException e) {                e.printStackTrace();            } catch (IOException e) {                e.printStackTrace();            }        } catch (RuntimeException e) {            requestObserver.onError(e);            throw e;        }        requestObserver.onCompleted();    }    @Async    public void appiumLogUpload(Long exeId, String deviceId, String appiumLogPath) {        log.info("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.info("Client response onNext");            }            @Override            public void onError(Throwable t) {                log.info("Client response onError");            }            @Override            public void onCompleted() {                log.info("Client response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).appiumLogUpload(responseObserver);        try {            File file = new File(appiumLogPath);            if (!file.exists()) {                log.info("Appium file does not exist");                return;            }            String filename = file.getName();            try {                FileInputStream fis = new FileInputStream(file);                BufferedInputStream bInputStream = new BufferedInputStream(fis);                FileChannel fc = fis.getChannel();                long total = fc.size();                int bufferSize = 512 * 1024; // 512k                byte[] buffer = new byte[bufferSize];                int tmp = 0;                int size = 0;                while ((tmp = bInputStream.read(buffer)) > 0) {                    ByteString byteString = ByteString.copyFrom(buffer);                    FileUploadRequest req = FileUploadRequest.newBuilder()                            .setName(filename)                            .setData(byteString)                            .setSize(total)                            .setOffset(size)                            .setExeId(exeId)                            .setDeviceId(deviceId)                            .setTimestamp(TimeUtil.getTimestampLong())                            .setToken(UserInfo.token)                            .build();                    requestObserver.onNext(req);                    size += tmp;                }            } catch (IOException e) {                e.printStackTrace();            }        } catch (RuntimeException e) {            requestObserver.onError(e);            throw e;        }        requestObserver.onCompleted();    }    @Async    public void imgUpload(Long exeId, String imgPath) {        log.info("tid: " +  Thread.currentThread().getId() + ", Will try to getBlob");        StreamObserver<CommonReply> responseObserver = new StreamObserver<CommonReply>() {            @Override            public void onNext(CommonReply value) {                log.info("Client response onNext");            }            @Override            public void onError(Throwable t) {                log.info("Client response onError");            }            @Override            public void onCompleted() {                log.info("Client response onCompleted");            }        };        StreamObserver<FileUploadRequest> requestObserver = Gvice.taskAsyncStub(serverChannel).imgUpload(responseObserver);        try {            File file = new File(imgPath);            if (!file.exists()) {                log.info("IMG file does not exist");                return;            }            String filename = file.getName();            try {                FileInputStream fis = new FileInputStream(file);                BufferedInputStream bInputStream = new BufferedInputStream(fis);                FileChannel fc = fis.getChannel();                long total = fc.size();                int bufferSize = 512 * 1024; // 512k                byte[] buffer = new byte[bufferSize];                int tmp = 0;                int size = 0;                while ((tmp = bInputStream.read(buffer)) > 0) {                    ByteString byteString = ByteString.copyFrom(buffer);                    FileUploadRequest req = FileUploadRequest.newBuilder()                            .setName(filename)                            .setData(byteString)                            .setSize(total)                            .setOffset(size)                            .setExeId(exeId)                            .setTimestamp(TimeUtil.getTimestampLong())                            .setToken(UserInfo.token)                            .build();                    requestObserver.onNext(req);                    size += tmp;                }//                Files.delete(Paths.get(imgPath));            } catch (IOException e) {                e.printStackTrace();            }        } catch (RuntimeException e) {            requestObserver.onError(e);            throw e;        }        requestObserver.onCompleted();    }    public void executorLogUpload(ExecutorLogInfo logInfo) {        ExecutorLogRequest request = ExecutorLogRequest.newBuilder()                .setDeviceId(logInfo.getDeviceId())                .setExeId(logInfo.getTaskId())                .setToken(UserInfo.token)                .setTimestamp(logInfo.getTime())                .setFlag(logInfo.getFlag())                .setMethodName(logInfo.getMethodName())                .setAction(logInfo.getAction())                .setArgs(logInfo.getArgs())                .setOrder(logInfo.getOrder())                .build();        Gvice.taskService(serverChannel).executorLogUpload(request);    }}