package com.testwa.distest.server.service.rpc;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.config.security.JwtTokenUtil;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.cache.mgr.SubscribeDeviceFuncMgr;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.websocket.WSFuncEnum;
import com.testwa.distest.server.websocket.service.PushCmdService;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.device.*;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by wen on 2017/06/09.
 */
@Slf4j
@GRpcService
public class DeviceGvice extends DeviceServiceGrpc.DeviceServiceImplBase{
    private final static DecimalFormat format = new DecimalFormat("###.0");

    @Autowired
    private SocketIOServer server;
    @Autowired
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private UserService userService;
    @Autowired
    private SubscribeDeviceFuncMgr subscribeMgr;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private PushCmdService pushCmdService;

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
    public void disconnect(DeviceStatusChangeRequest request, StreamObserver<CommonReply> responseObserver) {
        log.info("device {} disconnect", request.getDeviceId());
        deviceAuthMgr.offline(request.getDeviceId());
        final CommonReply.Builder replyBuilder = CommonReply.newBuilder().setMessage("OK ");
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void offline(DeviceStatusChangeRequest request, StreamObserver<CommonReply> responseObserver) {
        log.info("device {} offline", request.getDeviceId());
        deviceAuthMgr.offline(request.getDeviceId());
        final CommonReply.Builder replyBuilder = CommonReply.newBuilder().setMessage("OK ");
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void online(DeviceStatusChangeRequest request, StreamObserver<CommonReply> responseObserver) {
        log.info("device {} online", request.getDeviceId());
        deviceAuthMgr.online(request.getDeviceId());
        final CommonReply.Builder replyBuilder = CommonReply.newBuilder().setMessage("OK ");
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     *@Description: android设备连接
     *@Param: [request, responseObserver]
     *@Return: void
     *@Author: wen
     *@Date: 2018/5/7
     */
//    @Override
//    public void connect(ConnectedRequest request, StreamObserver<CommonReply> responseObserver) {
//        log.info("device {} connected", request.getDeviceId());
//        String username = jwtTokenUtil.getUsernameFromToken(request.getToken());
//        User user = userService.findByUsername(username);
//        Device device = new Device();
//        device.setBrand(request.getBrand());
//        device.setCpuabi(request.getCpuabi());
//        device.setDensity(request.getDensity());
//        device.setDeviceId(request.getDeviceId());
//        device.setHeight(request.getHeight());
//        device.setHost(request.getHost());
//        device.setModel(request.getModel());
//        device.setOsName(request.getOsName());
//        device.setOsVersion(request.getVersion());
//        device.setSdk(request.getSdk());
//        device.setWidth(request.getWidth());
//        device.setLastUserId(user.getId());
//        device.setLastUserToken(request.getToken());
//        device.setPhoneOS(DB.PhoneOS.ANDROID);
//        // 连接上来的设备设置为在线状态
//        device.setOnlineStatus(DB.PhoneOnlineStatus.ONLINE);
//        // 设置为空闲状态
//        device.setWorkStatus(DB.PhoneWorkStatus.FREE);
//
//        Device deviceBase = deviceService.findByDeviceId(request.getDeviceId());
//        if(deviceBase == null){
//            deviceService.insertAndroid(device);
//        }else{
//            deviceService.updateAndroid(device);
//        }
//        deviceAuthMgr.online(request.getDeviceId());
//
//        final CommonReply.Builder replyBuilder = CommonReply.newBuilder().setMessage("OK ");
//        responseObserver.onNext(replyBuilder.build());
//        responseObserver.onCompleted();
//    }


    @Override
    public void logcat(LogcatRequest request, StreamObserver<CommonReply> responseObserver) {
        String serial = request.getSerial();
        Set<String> sessions = subscribeMgr.getSubscribes(serial, WSFuncEnum.LOGCAT.getValue());
        byte[] data = request.getContent().toByteArray();
        for(String sessionId : sessions){
            SocketIOClient client = server.getClient(UUID.fromString(sessionId));
            if(client != null){
                String ds = new String(data);
                ds = ds.replace("\0", "");
                client.sendEvent("logcat", ds.trim());
            }else{
                log.info("[logcat] client is not found");
                subscribeMgr.delSubscribe(serial, WSFuncEnum.LOGCAT.getValue(), sessionId);
            }
        }

        final CommonReply.Builder replyBuilder = CommonReply.newBuilder().setMessage("OK ");
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }


    @Override
    public void screen(ScreenCaptureRequest request, StreamObserver<CommonReply> responseObserver) {
        String serial = request.getSerial();
        Set<String> sessions = subscribeMgr.getSubscribes(serial, WSFuncEnum.SCREEN.getValue());
        for(String sessionId : sessions){
            SocketIOClient client = server.getClient(UUID.fromString(sessionId));
            if(client != null){
                byte[] data = request.getImg().toByteArray();
                double i = (data.length / (1024.0));
                log.debug("SCREEN REC: data length is {} KB", format.format(i));
                client.sendEvent("minicap", data);
            }else{
                log.info("[screen] client is not found");
                subscribeMgr.delSubscribe(serial, WSFuncEnum.SCREEN.getValue(), sessionId);
            }
        }

        final CommonReply.Builder replyBuilder = CommonReply.newBuilder().setMessage("OK ");
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }
}
