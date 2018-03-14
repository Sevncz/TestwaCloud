package com.testwa.distest.server.service.rpc;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.config.security.JwtTokenUtil;
import com.testwa.distest.server.entity.DeviceAndroid;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.cache.mgr.SubscribeMgr;
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
    private SubscribeMgr subscribeMgr;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private PushCmdService pushCmdService;

    @Override
    public void all(DevicesRequest request, StreamObserver<CommonReply> responseObserver) {
        String token = request.getUserId();
        List<io.rpc.testwa.device.Device> l = request.getDeviceList();
        String username = jwtTokenUtil.getUsernameFromToken(token);
        User user = userService.findByUsername(username);
        for(io.rpc.testwa.device.Device device : l){
            handleDevice(device, user.getId());
        }
    }

    private void handleDevice(io.rpc.testwa.device.Device device, Long userId) {
        DeviceAndroid deviceAndroid = new DeviceAndroid();
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
        if("ON".equals(device.getStatus().name().toUpperCase())){
            deviceAuthMgr.online(device.getDeviceId());
            deviceAndroid.setOnlineStatus(DB.PhoneOnlineStatus.ONLINE);
        }
        if("OFF".equals(device.getStatus().name().toUpperCase())){
            deviceAuthMgr.offline(deviceAndroid.getDeviceId());
            deviceAndroid.setOnlineStatus(DB.PhoneOnlineStatus.OFFLINE);
        }
        Device deviceBase = deviceService.findByDeviceId(device.getDeviceId());
        if(deviceBase == null){
            deviceService.insertAndroid(deviceAndroid);
        }else{
            deviceService.updateAndroid(deviceAndroid);
        }
    }

    @Override
    public void disconnect(DisconnectedRequest request, StreamObserver<CommonReply> responseObserver) {
        log.info("device disconnect, {}", request.getDeviceId());
        deviceAuthMgr.offline(request.getDeviceId());
    }

    @Override
    public void offline(DisconnectedRequest request, StreamObserver<CommonReply> responseObserver) {
        log.info("device offline, {}", request.getDeviceId());
        deviceAuthMgr.offline(request.getDeviceId());
    }

    @Override
    public void connect(ConnectedRequest request, StreamObserver<CommonReply> responseObserver) {
        log.info("device connected, {}", request.getDeviceId());
        String username = jwtTokenUtil.getUsernameFromToken(request.getToken());
        User user = userService.findByUsername(username);
        DeviceAndroid deviceAndroid = new DeviceAndroid();
        deviceAndroid.setBrand(request.getBrand());
        deviceAndroid.setCpuabi(request.getCpuabi());
        deviceAndroid.setDensity(request.getDensity());
        deviceAndroid.setDeviceId(request.getDeviceId());
        deviceAndroid.setHeight(request.getHeight());
        deviceAndroid.setHost(request.getHost());
        deviceAndroid.setModel(request.getModel());
        deviceAndroid.setOsName(request.getOsName());
        deviceAndroid.setOsVersion(request.getVersion());
        deviceAndroid.setSdk(request.getSdk());
        deviceAndroid.setWidth(request.getWidth());
        deviceAndroid.setLastUserId(user.getId());
        deviceAndroid.setLastUserToken(request.getToken());
        // 连接上来的设备设置为在线状态
        deviceAndroid.setOnlineStatus(DB.PhoneOnlineStatus.ONLINE);
        // 设置为空闲状态
        deviceAndroid.setWorkStatus(DB.PhoneWorkStatus.FREE);

        Device deviceBase = deviceService.findByDeviceId(request.getDeviceId());
        if(deviceBase == null){
            deviceService.insertAndroid(deviceAndroid);
        }else{
            deviceService.updateAndroid(deviceAndroid);
        }
        deviceAuthMgr.online(request.getDeviceId());
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
            }else{
                subscribeMgr.delSubscribe(serial, WSFuncEnum.SCREEN.getValue(), sessionId);
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
                double i = (data.length / (1024.0));
                log.debug("SCREEN REC: data length is {} KB", format.format(i));
                client.sendEvent("minicap", data);
            }else{
                subscribeMgr.delSubscribe(serial, WSFuncEnum.SCREEN.getValue(), sessionId);
            }
        }

        final CommonReply.Builder replyBuilder = CommonReply.newBuilder().setMessage("OK ");
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }
}
