package com.testwa.distest.client.device.remote;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.testwa.distest.client.device.listener.callback.remote.ScreenObserver;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.device.*;
import io.rpc.testwa.agent.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.client.GrpcClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;

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

    public void sendCapture(String filename, String deviceId) {
        try {
            byte[] screeByte = Files.readAllBytes(Paths.get(filename));
            ScreenshotEvent request = ScreenshotEvent.newBuilder()
                    .setSerial(deviceId)
                    .setImg(ByteString.copyFrom(screeByte))
                    .build();
            MonitorServiceGrpc.MonitorServiceFutureStub monitorServiceFutureStub = MonitorServiceGrpc.newFutureStub(channel);
            ListenableFuture<Status> replyListenableFuture = monitorServiceFutureStub.screenshot(request);
            Status status = replyListenableFuture.get();
            // TODO 可以查看返回的消息
            log.debug(status.getStatus());
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
