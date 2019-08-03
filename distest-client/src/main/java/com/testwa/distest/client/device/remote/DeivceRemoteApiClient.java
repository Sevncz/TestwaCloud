package com.testwa.distest.client.device.remote;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.testwa.distest.client.component.stfagent.DevDisplay;
import com.testwa.distest.client.device.listener.callback.remote.ScreenObserver;
import com.testwa.distest.client.ios.IOSApp;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.device.*;
import io.rpc.testwa.agent.*;
import jp.co.cyberagent.stf.proto.Wire;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.client.GrpcClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 设备远程API客户端
 * @author wen
 * @create 2019-05-23 18:27
 */
@Slf4j
@Service
public class DeivceRemoteApiClient {
    @GrpcClient("grpc-server")
    private ManagedChannel channel;

    /**
     * @Description: 设备状态修改
     * @Param: [deviceId, status]
     * @Return: void
     * @Author wen
     * @Date 2019/5/23 18:33
     */
    public void stateChange(String deviceId, DeviceStatusChangeRequest.LineStatus status) {
        DeviceStatusChangeRequest request = DeviceStatusChangeRequest.newBuilder()
                .setDeviceId(deviceId)
                .setStatus(status)
                .build();
        DeviceServiceGrpc.DeviceServiceFutureStub deviceServiceFutureStub = DeviceServiceGrpc.newFutureStub(channel);
        ListenableFuture<CommonReply> reply = deviceServiceFutureStub.stateChange(request);
        try {
            CommonReply result = reply.get();
            result.getMessage();
        } catch (InterruptedException | ExecutionException e) {
            log.info("{} 同步状态失败", deviceId, e);
        }
    }

    /**
     * grpc
     */
    public void registerToServer(ClientInfo request, StreamObserver<Message> observer) {


        PushServiceGrpc.PushServiceStub pushStub = PushServiceGrpc.newStub(channel);
        pushStub.registerToServer(request, observer);
    }

    public String subscribe(ClientInfo request, String topic){
        TopicInfo topicInfo = TopicInfo.newBuilder().setTopicName(topic).setClientInfo(request).build();
        PushServiceGrpc.PushServiceBlockingStub PushServiceBlockingStub = PushServiceGrpc.newBlockingStub(channel);
        Status status = PushServiceBlockingStub.subscribe(topicInfo);
        return status.getStatus();
    }

    public String cancel(ClientInfo request, String topic){
        TopicInfo topicInfo = TopicInfo.newBuilder().setTopicName(topic).setClientInfo(request).build();
        PushServiceGrpc.PushServiceBlockingStub PushServiceBlockingStub = PushServiceGrpc.newBlockingStub(channel);
        Status status = PushServiceBlockingStub.cancel(topicInfo);
        return status.getStatus();
    }

    public String logoutFromServer(ClientInfo request){
        PushServiceGrpc.PushServiceBlockingStub PushServiceBlockingStub = PushServiceGrpc.newBlockingStub(channel);
        Status status = PushServiceBlockingStub.logoutFromServer(request);
        return status.getStatus();
    }

    public ScreenCaptureRequest getScreenCaptureRequest(byte[] frame, String deviceId) {
        return ScreenCaptureRequest.newBuilder()
                .setImg(ByteString.copyFrom(frame))
                .setSerial(deviceId)
                .build();
    }

    public StreamObserver<ScreenCaptureRequest> getScreenStub() {
        ScreenObserver screenObserver = new ScreenObserver();
        DeviceServiceGrpc.DeviceServiceStub deviceServiceStub = DeviceServiceGrpc.newStub(channel);
        StreamObserver<ScreenCaptureRequest> screenRequestObserver = deviceServiceStub.screen(screenObserver);
        return screenRequestObserver;
    }

    public void sendLogcatMessages(List<LogcatMessageRequest> logLines, String deviceId) {
        LogcatRequest request = LogcatRequest.newBuilder()
                .setSerial(deviceId)
                .addAllMessages(logLines)
                .build();
        DeviceServiceGrpc.DeviceServiceFutureStub deviceServiceFutureStub = DeviceServiceGrpc.newFutureStub(channel);
        ListenableFuture<CommonReply> replyListenableFuture = deviceServiceFutureStub.logcat(request);
        try {
            CommonReply reply = replyListenableFuture.get();
            // TODO 可以查看返回的消息
            log.debug(reply.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void sendLog(String logContent, String deviceId) {
        LogRequest request = LogRequest.newBuilder()
                .setSerial(deviceId)
                .setContent(logContent)
                .build();
        DeviceServiceGrpc.DeviceServiceFutureStub deviceServiceFutureStub = DeviceServiceGrpc.newFutureStub(channel);
        ListenableFuture<CommonReply> replyListenableFuture = deviceServiceFutureStub.log(request);
        try {
            CommonReply reply = replyListenableFuture.get();
            // TODO 可以查看返回的消息
            log.debug(reply.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendCapture(String filename, String deviceId) {
        try {
            byte[] screeByte = Files.readAllBytes(Paths.get(filename));
            sendCapture(screeByte, deviceId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendCapture(byte[] screeByte, String deviceId) {
        try {
            ScreenshotEvent request = ScreenshotEvent.newBuilder()
                    .setSerial(deviceId)
                    .setImg(ByteString.copyFrom(screeByte))
                    .build();
            MonitorServiceGrpc.MonitorServiceFutureStub monitorServiceFutureStub = MonitorServiceGrpc.newFutureStub(channel);
            ListenableFuture<Status> replyListenableFuture = monitorServiceFutureStub.screenshot(request);
            Status status = replyListenableFuture.get();
            // TODO 可以查看返回的消息
            log.debug(status.getStatus());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void handleEventDisplay(String deviceId, DevDisplay devDisplay) {

        try {
            DisplayEvent request = DisplayEvent.newBuilder()
                    .setSerial(deviceId)
                    .setDensity(devDisplay.getDensity())
                    .setFps(devDisplay.getFps())
                    .setHeight(devDisplay.getHeight())
                    .setWidth(devDisplay.getWidth())
                    .setRotation(devDisplay.getRotation())
                    .setXdpi(devDisplay.getXdpi())
                    .setYdpi(devDisplay.getYdpi())
                    .build();
            MonitorServiceGrpc.MonitorServiceFutureStub monitorServiceFutureStub = MonitorServiceGrpc.newFutureStub(channel);
            ListenableFuture<Status> replyListenableFuture = monitorServiceFutureStub.display(request);
            Status status = replyListenableFuture.get();
            // TODO 可以查看返回的消息
            log.debug(status.getStatus());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void handleEventBrowserApp(String deviceId, Wire.GetBrowsersResponse response) {

        try {
            List<BrowserApp> apps = new ArrayList<>();
            response.getAppsList().forEach(app -> {
                BrowserApp app3 = BrowserApp.newBuilder()
                        .setComponent(app.getComponent())
                        .setName(app.getName())
                        .setSystem(app.getSystem())
                        .setSelected(app.getSelected())
                        .build();
                apps.add(app3);
            });

            BrowserAppEvent request = BrowserAppEvent.newBuilder()
                    .setSerial(deviceId)
                    .setSelected(response.getSelected())
                    .setSuccess(response.getSuccess())
                    .addAllApps(apps)
                    .build();
            MonitorServiceGrpc.MonitorServiceFutureStub monitorServiceFutureStub = MonitorServiceGrpc.newFutureStub(channel);
            ListenableFuture<Status> replyListenableFuture = monitorServiceFutureStub.browserApp(request);
            Status status = replyListenableFuture.get();
            log.debug(status.getStatus());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void handleEventShellResult(String deviceId, String shellResult) {

        try {
            ShellEvent request = ShellEvent.newBuilder()
                    .setSerial(deviceId)
                    .setRet(shellResult)
                    .build();
            MonitorServiceGrpc.MonitorServiceFutureStub monitorServiceFutureStub = MonitorServiceGrpc.newFutureStub(channel);
            ListenableFuture<Status> replyListenableFuture = monitorServiceFutureStub.shell(request);
            Status status = replyListenableFuture.get();
            log.debug(status.getStatus());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * @Description: STF agent 安装情况
     * @Param: [serial]
     * @Return: void
     * @Author wen
     * @Date 2019-07-09 17:58
     */
    public void handleEventAgentInstall(String serial, boolean success) {
        try {
            StfAgentEvent request = StfAgentEvent.newBuilder()
                    .setSerial(serial)
                    .setSuccess(success)
                    .build();
            MonitorServiceGrpc.MonitorServiceFutureStub monitorServiceFutureStub = MonitorServiceGrpc.newFutureStub(channel);
            ListenableFuture<Status> replyListenableFuture = monitorServiceFutureStub.stfAgentInstallEvent(request);
            Status status = replyListenableFuture.get();
            log.debug(status.getStatus());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    /**
     * @Description: 返回App列表
     * @Param: [serial, apps]
     * @Return: void
     * @Author wen
     * @Date 2019-07-31 14:23
     */
    public void handleEventAppList(String serial, List<IOSApp> apps) {

        List<AgentApp> agentApps = apps.stream().map( app -> AgentApp.newBuilder()
                .setAppName(app.getAppName())
                .setAppVersion(app.getAppVersion())
                .setBundleId(app.getBundleId())
                .build()).collect(Collectors.toList());

        AppListEvent event = AppListEvent.newBuilder()
                .setSerial(serial)
                .addAllApps(agentApps)
                .build();

        try {
            MonitorServiceGrpc.MonitorServiceFutureStub monitorServiceFutureStub = MonitorServiceGrpc.newFutureStub(channel);
            ListenableFuture<Status> replyListenableFuture = monitorServiceFutureStub.appList(event);
            Status status = replyListenableFuture.get();
            log.debug(status.getStatus());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }
}
