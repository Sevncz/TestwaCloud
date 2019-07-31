package com.testwa.distest.server.rpc;import com.alibaba.fastjson.JSON;import com.corundumstudio.socketio.SocketIOClient;import com.corundumstudio.socketio.SocketIOServer;import com.google.protobuf.ByteString;import com.testwa.distest.server.service.cache.mgr.DeviceLockCache;import io.grpc.stub.StreamObserver;import io.rpc.testwa.agent.*;import lombok.extern.slf4j.Slf4j;import net.devh.springboot.autoconfigure.grpc.server.GrpcService;import org.apache.commons.lang3.StringUtils;import org.springframework.beans.factory.annotation.Autowired;import java.util.List;import java.util.UUID;import java.util.stream.Collectors;import static io.grpc.stub.ClientCalls.blockingUnaryCall;/** * @Program: 控制面板服务 * @Description: pushtest * @Author: wen * @Create: 2018-05-08 15:21 **/@Slf4j@GrpcService(MonitorServiceGrpc.class)public class MonitorServiceGvice extends MonitorServiceGrpc.MonitorServiceImplBase {    @Autowired    private DeviceLockCache deviceLockCache;    @Autowired    private SocketIOServer server;    @Override    public void screenshot(ScreenshotEvent request, StreamObserver<Status> responseObserver) {        String socketClientId = deviceLockCache.getValue(request.getSerial());        if(StringUtils.isNotEmpty(socketClientId)) {            SocketIOClient client = server.getClient(UUID.fromString(socketClientId));            if(client.isChannelOpen()) {                ByteString img = request.getImg();                client.sendEvent("screenshot", img.toByteArray());            }        }        responseObserver.onNext(Status.newBuilder().setStatus("OK").build());        responseObserver.onCompleted();    }    @Override    public void shell(ShellEvent request, StreamObserver<Status> responseObserver) {        String socketClientId = deviceLockCache.getValue(request.getSerial());        if(StringUtils.isNotEmpty(socketClientId)) {            SocketIOClient client = server.getClient(UUID.fromString(socketClientId));            if(client.isChannelOpen()) {                client.sendEvent("shell", request.getRet());            }        }        responseObserver.onNext(Status.newBuilder().setStatus("OK").build());        responseObserver.onCompleted();    }    @Override    public void display(DisplayEvent request, StreamObserver<Status> responseObserver) {        String socketClientId = deviceLockCache.getValue(request.getSerial());        if(StringUtils.isNotEmpty(socketClientId)) {            SocketIOClient client = server.getClient(UUID.fromString(socketClientId));            if(client.isChannelOpen()) {                DevDisplay screen = new DevDisplay(request);                client.sendEvent("display", JSON.toJSONString(screen));            }        }        responseObserver.onNext(Status.newBuilder().setStatus("OK").build());        responseObserver.onCompleted();    }    @Override    public void battery(BatteryEvent request, StreamObserver<Status> responseObserver) {        String socketClientId = deviceLockCache.getValue(request.getSerial());        if(StringUtils.isNotEmpty(socketClientId)) {            SocketIOClient client = server.getClient(UUID.fromString(socketClientId));            if(client.isChannelOpen()) {                DevBattery screen = new DevBattery(request);                client.sendEvent("battery", JSON.toJSONString(screen));            }        }        responseObserver.onNext(Status.newBuilder().setStatus("OK").build());        responseObserver.onCompleted();    }    @Override    public void browserApp(BrowserAppEvent request, StreamObserver<Status> responseObserver) {        String socketClientId = deviceLockCache.getValue(request.getSerial());        if(StringUtils.isNotEmpty(socketClientId)) {            SocketIOClient client = server.getClient(UUID.fromString(socketClientId));            if(client.isChannelOpen()) {                DevBrowserAppPackage browserApp = new DevBrowserAppPackage(request);                client.sendEvent("browser_app", JSON.toJSONString(browserApp));            }        }        responseObserver.onNext(Status.newBuilder().setStatus("OK").build());        responseObserver.onCompleted();    }    @Override    public void stfAgentInstallEvent(StfAgentEvent request, StreamObserver<Status> responseObserver) {        String socketClientId = deviceLockCache.getValue(request.getSerial());        if(StringUtils.isNotEmpty(socketClientId)) {            SocketIOClient client = server.getClient(UUID.fromString(socketClientId));            if(client.isChannelOpen()) {                client.sendEvent("stfagent_success", request.getSuccess());            }        }        responseObserver.onNext(Status.newBuilder().setStatus("OK").build());        responseObserver.onCompleted();    }    @Override    public void appList(AppListEvent request, StreamObserver<Status> responseObserver) {        String socketClientId = deviceLockCache.getValue(request.getSerial());        if(StringUtils.isNotEmpty(socketClientId)) {            SocketIOClient client = server.getClient(UUID.fromString(socketClientId));            if(client.isChannelOpen()) {                List<IOSApp> apps = request.getAppsList().stream().map(IOSApp::new).collect(Collectors.toList());                client.sendEvent("app_list", JSON.toJSONString(apps));            }        }        responseObserver.onNext(Status.newBuilder().setStatus("OK").build());        responseObserver.onCompleted();    }}