package com.testwa.distest.server.rpc.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.distest.server.LogInterceptor;
import com.testwa.distest.server.entity.DeviceAndroid;
import com.testwa.distest.server.entity.DeviceBase;
import com.testwa.distest.server.service.cache.mgr.SubscribeMgr;
import com.testwa.distest.server.web.auth.jwt.JwtTokenUtil;
import com.testwa.distest.server.service.cache.mgr.DeviceCacheMgr;
import com.testwa.distest.server.websocket.WSFuncEnum;
import com.testwa.distest.server.rpc.GRpcService;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.device.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by wen on 2017/06/09.
 */
@GRpcService(interceptors = { LogInterceptor.class })
public class DeviceGvice extends DeviceServiceGrpc.DeviceServiceImplBase{
    private static final Logger log = LoggerFactory.getLogger(DeviceGvice.class);

    @Autowired
    private SocketIOServer server;
    @Autowired
    private DeviceCacheMgr deviceCacheMgr;
    @Autowired
    private SubscribeMgr subscribeMgr;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public void all(DevicesRequest request, StreamObserver<CommonReply> responseObserver) {
        String token = request.getUserId();
        List<Device> l = request.getDeviceList();
        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        for(Device device : l){
            handleDevice(device, userId);
        }
    }

    private void handleDevice(Device device, Long userId) {
        // 看redis里有没有设备,没有则保存
        DeviceBase deviceBase = deviceCacheMgr.getDeviceContent(device.getDeviceId());
        DeviceAndroid deviceAndroid = null;
        if(deviceBase == null){
            deviceAndroid = new DeviceAndroid();
            log.info("Save a new device to db, deviceId: {}.", device.getDeviceId());
        }else{
            deviceAndroid = (DeviceAndroid) deviceBase;
        }
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

//        UserDeviceHis udh = userDeviceHisService.findByUserIdAndDeviceId(userId, device.getDeviceId());
//        if(udh == null){
//            udh = new UserDeviceHis(userId, tDevice);
//            userDeviceHisService.save(udh);
//        }

        // 修改设备状态
//        tDevice.setStatus(device.getStatus().name().toUpperCase());
//        deviceService.save(tDevice);
//        udh.setD_status(device.getStatus().name().toUpperCase());
//        userDeviceHisService.save(udh);

        if("ON".equals(device.getStatus().name().toUpperCase())){
            deviceCacheMgr.saveDeviceContent(deviceAndroid);
        }
        if("OFF".equals(device.getStatus().name().toUpperCase())){
            deviceCacheMgr.delDeviceContent(deviceAndroid.getDeviceId());
        }
    }

    @Override
    public void disconnect(NoUsedDeviceRequest request, StreamObserver<CommonReply> responseObserver) {
        log.info("device disconnect, {}", request.getDeviceId());
        deviceCacheMgr.delDeviceContent(request.getDeviceId());
        // 修改设备状态为OFF 数据库不需要 off 状态标识
//        DeviceAndroid tDevice = deviceService.getDeviceById(request.getDeviceId());
//        tDevice.setStatus("OFF");
//        deviceService.save(tDevice);
    }

    @Override
    public void offline(NoUsedDeviceRequest request, StreamObserver<CommonReply> responseObserver) {
        log.info("device offline, {}", request.getDeviceId());
        deviceCacheMgr.delDeviceContent(request.getDeviceId());
    }

    @Override
    public void logcat(LogcatRequest request, StreamObserver<CommonReply> responseObserver) {
        String serial = request.getSerial();
        Set<String> sessions = subscribeMgr.getSubscribes(serial, WSFuncEnum.LOGCAT.getValue());
        byte[] data = request.getContent().toByteArray();
        for(String sessionId : sessions){
            SocketIOClient client = server.getClient(UUID.fromString(sessionId));
            if(client != null){
                client.sendEvent("logcat", new String(data));
            }
        }
        log.debug(" GET data length {}", data.length);

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
                client.sendEvent("minicap", data);
            }
        }

        final CommonReply.Builder replyBuilder = CommonReply.newBuilder().setMessage("OK ");
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }
}
