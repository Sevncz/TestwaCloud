package com.testwa.distest.client.device.remote;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.testwa.distest.client.callback.remote.ScreenObserver;
import com.testwa.distest.client.device.listener.IDeviceRemoteCommandListener;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.device.*;
import io.rpc.testwa.push.ClientInfo;
import io.rpc.testwa.push.PushGrpc;
import io.rpc.testwa.push.Status;
import io.rpc.testwa.push.TopicInfo;
import io.rpc.testwa.task.TaskServiceGrpc;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 设备远程API客户端
 * @author wen
 * @create 2019-05-23 18:27
 */
@Slf4j
public class DeivceRemoteApiClient {
    private final ManagedChannel channel;
    private final DeviceServiceGrpc.DeviceServiceFutureStub deviceServiceFutureStub;
    private final DeviceServiceGrpc.DeviceServiceStub deviceServiceStub;
    private final DeviceServiceGrpc.DeviceServiceBlockingStub deviceServiceBlockingStub;

    private final TaskServiceGrpc.TaskServiceFutureStub taskServiceFutureStub;
    private final TaskServiceGrpc.TaskServiceStub taskServiceStub;
    private final TaskServiceGrpc.TaskServiceBlockingStub taskServiceBlockingStub;

    private final PushGrpc.PushFutureStub pushFutureStub;
    private final PushGrpc.PushStub pushStub;
    private final PushGrpc.PushBlockingStub pushBlockingStub;

    private final StreamObserver<ScreenCaptureRequest> screenRequestObserver;

    public DeivceRemoteApiClient(String host, int port){
       this.channel = NettyChannelBuilder.forAddress(host, port)
                .build();
        this.deviceServiceFutureStub = DeviceServiceGrpc.newFutureStub(channel);
        this.deviceServiceStub = DeviceServiceGrpc.newStub(channel);
        this.deviceServiceBlockingStub = DeviceServiceGrpc.newBlockingStub(channel);


        this.taskServiceFutureStub = TaskServiceGrpc.newFutureStub(channel);
        this.taskServiceStub = TaskServiceGrpc.newStub(channel);
        this.taskServiceBlockingStub = TaskServiceGrpc.newBlockingStub(channel);


        this.pushFutureStub = PushGrpc.newFutureStub(channel);
        this.pushStub = PushGrpc.newStub(channel);
        this.pushBlockingStub = PushGrpc.newBlockingStub(channel);


        ScreenObserver screenObserver = new ScreenObserver();
        this.screenRequestObserver = this.deviceServiceStub.screen(screenObserver);
    }

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
    public void registerToServer(ClientInfo request, IDeviceRemoteCommandListener deviceCommandListener) {
        this.pushStub.registerToServer(request, deviceCommandListener);
    }

    public String subscribe(ClientInfo request, String topic){
        TopicInfo topicInfo = TopicInfo.newBuilder().setTopicName(topic).setClientInfo(request).build();
        Status status = this.pushBlockingStub.subscribe(topicInfo);
        return status.getStatus();
    }

    public String cancel(ClientInfo request, String topic){
        TopicInfo topicInfo = TopicInfo.newBuilder().setTopicName(topic).setClientInfo(request).build();
        Status status = this.pushBlockingStub.cancel(topicInfo);
        return status.getStatus();
    }

    public String logoutFromServer(ClientInfo request){
        Status status = this.pushBlockingStub.logoutFromServer(request);
        return status.getStatus();
    }


    /**
     * @Description: 发送屏幕数据流
     * @Param: [frame, deviceId]
     * @Return: void
     * @Author wen
     * @Date 2019/5/23 20:58
     */
    public void sendScreen(byte[] frame, String deviceId) {

        try {
            ScreenCaptureRequest request = ScreenCaptureRequest.newBuilder()
                    .setImg(ByteString.copyFrom(frame))
                    .setSerial(deviceId)
                    .build();

            this.screenRequestObserver.onNext(request);
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
        }
    }

    public void sendLogcatMessages(List<LogcatMessageRequest> logLines, String deviceId) {
        LogcatRequest request = LogcatRequest.newBuilder()
                .setSerial(deviceId)
                .addAllMessages(logLines)
                .build();
        this.deviceServiceFutureStub.logcat(request);
    }
}
