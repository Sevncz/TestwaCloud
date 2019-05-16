package com.testwa.distest.server.rpc;

import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.config.security.JwtTokenUtil;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.cache.mgr.SubscribeDeviceFuncMgr;
import com.testwa.distest.server.service.cache.queue.LogQueue;
import com.testwa.distest.server.service.cache.queue.ScreenProjectionQueue;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.mgr.DeviceOnlineMgr;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.device.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.server.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by wen on 2017/06/09.
 */
@Slf4j
@GrpcService(DeviceServiceGrpc.class)
public class DeviceGvice extends DeviceServiceGrpc.DeviceServiceImplBase{
    private final static DecimalFormat format = new DecimalFormat("###.0");

    @Autowired
    private DeviceOnlineMgr deviceOnlineMgr;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private ScreenProjectionQueue screenStreamQueue;
    @Autowired
    private LogQueue logQueue;

    @Override
    public void all(DevicesRequest request, StreamObserver<CommonReply> responseObserver) {
        log.info("客户端上报device，进行更新和保存操作");
        String token = request.getUserId();
        List<io.rpc.testwa.device.Device> l = request.getDeviceList();
        String username = jwtTokenUtil.getUsernameFromToken(token);
        User user = userService.findByUsername(username);
        for(io.rpc.testwa.device.Device device : l){
            handleDevice(device, user.getId());
        }
        final CommonReply.Builder replyBuilder = CommonReply.newBuilder().setMessage("OK ");
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }

    private void handleDevice(io.rpc.testwa.device.Device device, Long userId) {
        Device deviceAndroid = new Device();
        deviceAndroid.setBrand(device.getBrand());
        deviceAndroid.setCpuabi(device.getCpuabi());
        deviceAndroid.setDensity(device.getDensity());
        deviceAndroid.setDeviceId(device.getDeviceId());
        deviceAndroid.setHeight(device.getHeight());
        deviceAndroid.setHost(device.getHost());
        deviceAndroid.setModel(device.getModel());
        deviceAndroid.setOsName(device.getOsName());
        deviceAndroid.setSdk(device.getSdk());
        deviceAndroid.setWidth(device.getWidth());
        deviceAndroid.setLastUserId(userId);
        deviceAndroid.setOsVersion(device.getVersion());
        Device deviceBase = deviceService.findByDeviceId(device.getDeviceId());
        if(deviceBase == null){
            deviceService.insertAndroid(deviceAndroid);
        }else{
            deviceService.updateAndroid(deviceAndroid);
        }
    }

    @Override
    public void stateChange(DeviceStatusChangeRequest request, StreamObserver<CommonReply> responseObserver) {
        DB.PhoneOnlineStatus status = DB.PhoneOnlineStatus.valueOf(request.getStatusValue());
        if(DeviceStatusChangeRequest.LineStatus.ONLINE.equals(request.getStatus())) {
            deviceOnlineMgr.online(request.getDeviceId());
        }else if(DeviceStatusChangeRequest.LineStatus.OFFLINE.equals(request.getStatus()) ||
            DeviceStatusChangeRequest.LineStatus.DISCONNECTED.equals(request.getStatus()) ) {
            deviceOnlineMgr.offline(request.getDeviceId(), status);
        }else{
            deviceOnlineMgr.otherStatus(request.getDeviceId(), status);
        }
        final CommonReply.Builder replyBuilder = CommonReply.newBuilder().setMessage("OK ");
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }


    @Override
    public void logcat(LogcatRequest request, StreamObserver<CommonReply> responseObserver) {
        String serial = request.getSerial();
        request.getMessagesList().forEach(m -> {
            logQueue.push(serial, m.toByteArray());
        });
        final CommonReply.Builder replyBuilder = CommonReply.newBuilder().setMessage("OK ");
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }


    @Override
    public StreamObserver<ScreenCaptureRequest> screen(StreamObserver<CommonReply> responseObserver) {
        return new StreamObserver<ScreenCaptureRequest>() {

            @Override
            public void onNext(ScreenCaptureRequest request) {
                String serial = request.getSerial();
                screenStreamQueue.push(serial, request.getImg());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(CommonReply.newBuilder().setMessage("OK ")
                        .build());
                responseObserver.onCompleted();
            }
        };
    }
}
