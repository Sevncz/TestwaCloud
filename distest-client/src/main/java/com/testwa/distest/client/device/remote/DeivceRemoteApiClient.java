package com.testwa.distest.client.device.remote;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.testwa.distest.client.device.listener.IDeviceRemoteCommandListener;
import com.testwa.distest.client.device.listener.callback.remote.ScreenObserver;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.device.*;
import io.rpc.testwa.push.ClientInfo;
import io.rpc.testwa.push.PushGrpc;
import io.rpc.testwa.push.Status;
import io.rpc.testwa.push.TopicInfo;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.client.GrpcClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
    private static final ConcurrentLinkedQueue<ScreenCaptureRequest> SCREEN_IMG_QUQUE = new ConcurrentLinkedQueue<>();
    private ExecutorService pool;

    private DeivceRemoteApiClient() {
        log.info("start screen send quque");
        ScreenSendTask task = new ScreenSendTask();
        ScreenTaskFactory factory = new ScreenTaskFactory(task);
        pool = Executors.newSingleThreadExecutor(factory);
        pool.execute(task);
    }

    @PreDestroy
    public void destroy(){
        pool.shutdown();
    }

    class ScreenTaskFactory implements ThreadFactory {
        private ScreenSendTask screenSendTask;

        public ScreenTaskFactory(ScreenSendTask task) {
            super();
            this.screenSendTask = task;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setUncaughtExceptionHandler((t, e) -> {
                pool = Executors.newSingleThreadExecutor(new ScreenTaskFactory(screenSendTask));
                pool.execute(screenSendTask);
            });
            return thread;
        }
    }

    /**
     * @Description: screen发送线程
     * @Param: []
     * @Return: void
     * @Author wen
     * @Date 2019/6/10 16:03
     */
    class ScreenSendTask implements Runnable {
        @Override
        public void run() {
            ScreenObserver screenObserver = new ScreenObserver();
            DeviceServiceGrpc.DeviceServiceStub deviceServiceStub = DeviceServiceGrpc.newStub(channel);
            StreamObserver<ScreenCaptureRequest> screenRequestObserver = deviceServiceStub.screen(screenObserver);
            while(true) {
                try {
                    synchronized (SCREEN_IMG_QUQUE) {
                        if(!SCREEN_IMG_QUQUE.isEmpty()) {
                            ScreenCaptureRequest request = SCREEN_IMG_QUQUE.poll();
                            screenRequestObserver.onNext(request);
                        }else{
                            TimeUnit.MILLISECONDS.sleep(1);
                        }
                    }
                }catch (Exception e) {
                    log.error("屏幕同步线程发生未知异常", e);
                }
            }
        }
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
    public void registerToServer(ClientInfo request, IDeviceRemoteCommandListener deviceCommandListener) {
        PushGrpc.PushStub pushStub = PushGrpc.newStub(channel);
        pushStub.registerToServer(request, deviceCommandListener);
    }

    public String subscribe(ClientInfo request, String topic){
        TopicInfo topicInfo = TopicInfo.newBuilder().setTopicName(topic).setClientInfo(request).build();
        PushGrpc.PushBlockingStub pushBlockingStub = PushGrpc.newBlockingStub(channel);
        Status status = pushBlockingStub.subscribe(topicInfo);
        return status.getStatus();
    }

    public String cancel(ClientInfo request, String topic){
        TopicInfo topicInfo = TopicInfo.newBuilder().setTopicName(topic).setClientInfo(request).build();
        PushGrpc.PushBlockingStub pushBlockingStub = PushGrpc.newBlockingStub(channel);
        Status status = pushBlockingStub.cancel(topicInfo);
        return status.getStatus();
    }

    public String logoutFromServer(ClientInfo request){
        PushGrpc.PushBlockingStub pushBlockingStub = PushGrpc.newBlockingStub(channel);
        Status status = pushBlockingStub.logoutFromServer(request);
        return status.getStatus();
    }


    /**
     * @Description: 发送屏幕数据流
     * @Param: [frame, deviceId]
     * @Return: void
     * @Author wen
     * @Date 2019/5/23 20:58
     */
    public void saveScreen(byte[] frame, String deviceId) {
        try {
            ScreenCaptureRequest request = ScreenCaptureRequest.newBuilder()
                    .setImg(ByteString.copyFrom(frame))
                    .setSerial(deviceId)
                    .build();
            SCREEN_IMG_QUQUE.add(request);
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
}
