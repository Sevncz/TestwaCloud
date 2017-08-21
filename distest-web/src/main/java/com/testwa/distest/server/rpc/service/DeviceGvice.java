package com.testwa.distest.server.rpc.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.googlecode.protobuf.format.JsonFormat;
import com.testwa.core.utils.DeviceType;
import com.testwa.distest.server.LogInterceptor;
import com.testwa.distest.server.security.JwtTokenUtil;
import com.testwa.distest.server.websocket.WSFuncEnum;
import com.testwa.distest.server.rpc.GRpcService;
import com.testwa.distest.server.mvc.model.TDevice;
import com.testwa.distest.server.mvc.model.UserDeviceHis;
import com.testwa.distest.server.mvc.service.DeviceService;
import com.testwa.distest.server.mvc.service.UserDeviceHisService;
import com.testwa.distest.server.mvc.service.cache.RemoteClientService;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.device.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

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
    private RemoteClientService remoteClientService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private UserDeviceHisService userDeviceHisService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public void all(DevicesRequest request, StreamObserver<CommonReply> responseObserver) {
        JsonFormat jf = new JsonFormat();
        String token = request.getUserId();
        List<Device> l = request.getDeviceList();
        String userId = jwtTokenUtil.getUserIdFromToken(token);
        for(Device device : l){
            String fbJson = jf.printToString(device);
            if("ON".equals(device.getStatus().name().toUpperCase())){

                remoteClientService.saveDevice(userId, device.getDeviceId());

            }

            if("OFF".equals(device.getStatus().name().toUpperCase())){
                log.info("OFFLINE, device info is {}", fbJson);
                remoteClientService.delDevice(device.getDeviceId());
            }
            // 看数据库有没有设备,没有则保存
            TDevice tDevice = deviceService.getDeviceById(device.getDeviceId());
            if(tDevice == null){
                tDevice = new TDevice();
                tDevice.toEntity(device);
                tDevice.setType(DeviceType.ANDROID.getName());
                log.info("Save a new device to db, deviceId: {}.", device.getDeviceId());
            }

            UserDeviceHis udh = userDeviceHisService.findByUserIdAndDeviceId(userId, device.getDeviceId());
            if(udh == null){
                udh = new UserDeviceHis(userId, tDevice);
                userDeviceHisService.save(udh);
            }

            // 修改设备状态
            tDevice.setStatus(device.getStatus().name().toUpperCase());
            deviceService.save(tDevice);
            udh.setD_status(device.getStatus().name().toUpperCase());
            userDeviceHisService.save(udh);
        }
    }

    @Override
    public void disconnect(NoUsedDeviceRequest request, StreamObserver<CommonReply> responseObserver) {
        log.info("device disconnect, {}", request.getDeviceId());
        remoteClientService.delDevice(request.getDeviceId());
        // 修改设备状态为OFF
        TDevice tDevice = deviceService.getDeviceById(request.getDeviceId());
        tDevice.setStatus("OFF");
        deviceService.save(tDevice);

    }

    @Override
    public void offline(NoUsedDeviceRequest request, StreamObserver<CommonReply> responseObserver) {
        log.info("device offline, {}", request.getDeviceId());
        remoteClientService.delDevice(request.getDeviceId());
    }

    @Override
    public void logcat(LogcatRequest request, StreamObserver<CommonReply> responseObserver) {
        String serial = request.getSerial();
        Set<Object> sessions = remoteClientService.getSubscribes(serial, WSFuncEnum.LOGCAT.getValue());
        byte[] data = request.getContent().toByteArray();
        for(Object sessionId : sessions){
            SocketIOClient client = server.getClient(UUID.fromString((String)sessionId));
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
        Set<Object> sessions = remoteClientService.getSubscribes(serial, WSFuncEnum.SCREEN.getValue());
        for(Object sessionId : sessions){
            SocketIOClient client = server.getClient(UUID.fromString((String)sessionId));
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
