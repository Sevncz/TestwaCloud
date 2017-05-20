package com.testwa.distest.server.api.websocket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.testwa.core.utils.DeviceType;
import com.testwa.distest.server.config.EventConstant;
import com.testwa.distest.server.model.TestwaDevice;
import com.testwa.distest.server.model.UserDeviceHis;
import com.testwa.distest.server.service.TestwaDeviceService;
import com.testwa.distest.server.service.UserDeviceHisService;
import com.testwa.distest.server.service.redis.TestwaDeviceRedisService;
import io.grpc.testwa.device.Device;
import io.grpc.testwa.device.DevicesRequest;
import io.grpc.testwa.device.NoUsedDeviceRequest;
import io.grpc.testwa.device.ScreenCaptureRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DeviceHandler {
    private static final Logger log = LoggerFactory.getLogger(DeviceHandler.class);

    private final SocketIOServer server;

    @Autowired
    private Environment env;

    @Autowired
    private TestwaDeviceService testwaDeviceService;
    @Autowired
    private UserDeviceHisService userDeviceHisService;
    @Autowired
    private TestwaDeviceRedisService deviceRedisService;

    @Autowired
    public DeviceHandler(SocketIOServer server) {
        this.server = server;
    }

    private void clearNoUsedDevice(List<Device> devices, String clientSessionId) {
        Set<String> cacheDeviceIds = deviceRedisService.getDevicesBySessionId(clientSessionId);
        Set<String> receiveDeviceIds = new HashSet<>();
        for(Device d : devices){
            receiveDeviceIds.add(d.getDeviceId());
        }
        cacheDeviceIds.removeAll(receiveDeviceIds);
        for(String deviceId : cacheDeviceIds){
            deviceRedisService.clearNoUsedDevice(clientSessionId, deviceId);
        }
    }

    @OnEvent(value = "device")
    public void onEvent(SocketIOClient client, byte[] data, AckRequest ackRequest) {
        String username = client.getHandshakeData().getSingleUrlParam("username");
        log.info("username  ==== {}", username);

        Long start = System.currentTimeMillis();
        try {
            JsonFormat jf = new JsonFormat();
            DevicesRequest devices = DevicesRequest.parseFrom(data);
            String userId = devices.getUserId();
            List<Device> l = devices.getDeviceList();
            String clientSessionId = client.getSessionId().toString();
            clearNoUsedDevice(l, clientSessionId);

            for(Device device : l){
                String fbJson = jf.printToString(device);
                log.debug(String.format("connect client : %s, %s", fbJson, client.getSessionId().toString()));
                if("ON".equals(device.getStatus().name().toUpperCase())){
                    // 保存device - session的关系
                    deviceRedisService.addDeviceClientSession(device.getDeviceId(), client.getSessionId().toString());
                    // 保存session - devices的关系
                    deviceRedisService.addClientSessionDevices(clientSessionId, device.getDeviceId());
                }

                if("OFF".equals(device.getStatus().name().toUpperCase())){
                    log.info("OFFLINE, device info is {}", fbJson);
                    deviceRedisService.clearNoUsedDevice(clientSessionId, device.getDeviceId());
                }
                // 看数据库有没有设备,没有则保存
                TestwaDevice testwaDevice = testwaDeviceService.getDeviceById(device.getDeviceId());
                if(testwaDevice == null){
                    testwaDevice = new TestwaDevice();
                    testwaDevice.toEntity(device);
                    testwaDevice.setType(DeviceType.ANDROID.getName());
                    log.info("Save a new device to db, deviceId: {}.", device.getDeviceId());
                }

                UserDeviceHis udh = userDeviceHisService.findByUserIdAndDeviceId(userId, device.getDeviceId());
                if(udh == null){
                    udh = new UserDeviceHis(userId, testwaDevice);
                    userDeviceHisService.save(udh);
                }

                // 修改设备状态
                testwaDevice.setStatus(device.getStatus().name().toUpperCase());
                testwaDeviceService.save(testwaDevice);
                udh.setD_status(device.getStatus().name().toUpperCase());
                userDeviceHisService.save(udh);
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("Method onEvent error", e);
        }
        client.sendEvent("device_feedback", "success");
        Long end = System.currentTimeMillis();

        log.debug("接受设备信息, 花费时间 ------ {}ms", end - start);
    }


    @OnEvent(value = EventConstant.feedback_runninglog_screen)
    public void onScreenEvent(SocketIOClient client, byte[] data, AckRequest ackRequest) {

        try {
            ScreenCaptureRequest request =ScreenCaptureRequest.parseFrom(data);
            Path screenPath = Paths.get(env.getProperty("screeshot.path"), request.getName());
            File screenDir = screenPath.getParent().toFile();
            if(!screenDir.exists()){
                screenDir.mkdirs();
            }
            BufferedOutputStream stream =
                    new BufferedOutputStream(new FileOutputStream(screenPath.toFile()));
            stream.write(request.getImg().toByteArray());
            stream.close();
        } catch (IOException e) {
            log.error("Method onScreenEvent error", e);
        }

        client.sendEvent(EventConstant.feedback_runninglog_screen, "success");

    }

    @OnEvent(value = "deviceDisconnect")
    public void deviceDisconnect(SocketIOClient client, byte[] data, AckRequest ackRequest) {
        try {
            NoUsedDeviceRequest noUsedDevice =NoUsedDeviceRequest.parseFrom(data);
            log.info("device disconnect, {}", noUsedDevice.getDeviceId());
            deviceRedisService.clearNoUsedDevice(client.getSessionId().toString(), noUsedDevice.getDeviceId());
            // 修改设备状态为OFF
            TestwaDevice testwaDevice = testwaDeviceService.getDeviceById(noUsedDevice.getDeviceId());
            testwaDevice.setStatus("OFF");
            testwaDeviceService.save(testwaDevice);
        } catch (InvalidProtocolBufferException e) {
            log.error("InvalidProtocolBufferException", e);
        }
    }


    @OnEvent(value = "deviceOffLine")
    public void deviceOffLine(SocketIOClient client, byte[] data, AckRequest ackRequest) {
        try {
            NoUsedDeviceRequest noUsedDevice =NoUsedDeviceRequest.parseFrom(data);
            log.info("device offline, {}", noUsedDevice.getDeviceId());
            deviceRedisService.clearNoUsedDevice(client.getSessionId().toString(), noUsedDevice.getDeviceId());
        } catch (InvalidProtocolBufferException e) {
            log.error("InvalidProtocolBufferException", e);
        }
    }


//    @OnEvent(value = "showScreen")
//    public void showScreen(SocketIOClient client, byte[] data, AckRequest ackRequest) {
//        try {
//            ScreenCaptureRequest request =ScreenCaptureRequest.parseFrom(data);
//            log.info("deviceId: {}, img: {}", request.getSerial(), request.getImg().toByteArray());
//        } catch (InvalidProtocolBufferException e) {
//            log.error("InvalidProtocolBufferException", e);
//        }
//    }


}