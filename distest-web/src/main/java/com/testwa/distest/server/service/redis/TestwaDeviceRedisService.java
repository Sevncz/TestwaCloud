package com.testwa.distest.server.service.redis;

import io.grpc.testwa.device.ScreenCaptureRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * Created by wen on 2016/10/16.
 */
@Service
public class TestwaDeviceRedisService {
    private static final Logger log = LoggerFactory.getLogger(TestwaDeviceRedisService.class);
    private static String clientSessionDevicesKey = "client.session.devices.%s";
    private static final String devicesQueue = "t.devices";

    @Autowired
    private StringRedisTemplate template;

    public void addClientSessionDevices(String clientSessionId, String deviceId){
        /**
         * 保存session对应的device, one to many
         */
        template.opsForSet().add(String.format(clientSessionDevicesKey, clientSessionId), deviceId);
    }

    public void addDeviceClientSession(String deviceId, String sessionId) {
        /**
         * 保存device对应的session, one to one
         */
        template.opsForHash().put(devicesQueue, deviceId, sessionId);
    }

    public void delDeviceClientSession(String deviceId, String needDelSessionId) {
        /**
         * 删除device
         */
        String sessionId = (String) template.opsForHash().get(devicesQueue, deviceId);
        if(StringUtils.isNotBlank(needDelSessionId) &&  needDelSessionId.equals(sessionId)){

            template.opsForHash().delete(devicesQueue, deviceId);

        }

    }

    public void delClientSession(String sessionId, String deviceId) {
        /**
         * 删除session下的一个deviceId
         */
        template.opsForSet().remove(String.format(clientSessionDevicesKey, sessionId), deviceId);
    }


    public void clearNoUsedDevice(String clientSessionId, String deviceId) {
        log.info("clear no used device, {}", deviceId);
        delDeviceClientSession(deviceId, clientSessionId);
        delClientSession(clientSessionId, deviceId);
    }

    public Set<String> getDevicesBySessionId(String clientSessionId) {
        /**
         * 获得某个agent下的所有设备
         */
        return template.opsForSet().members(String.format(clientSessionDevicesKey, clientSessionId));
    }

    public String getSessionIdById(String deviceId) {
        return (String) template.opsForHash().get(devicesQueue, deviceId);
    }

    public void saveImgData(ScreenCaptureRequest request) {
        String deviceId = request.getSerial();
        String key = String.format("screenshoot.%s", deviceId);
        String base64Img = Base64.encodeBase64String(request.getImg().toByteArray());
        template.opsForList().leftPush(key, base64Img);
        if(template.opsForList().size(key) > 100){
            template.opsForList().rightPop(key);
        }
    }

    public byte[] getImgData(String deviceId) throws UnsupportedEncodingException {
        String key = String.format("screenshoot.%s", deviceId);
        String imgdata = template.opsForList().rightPop(key);

        byte[] imgData = new byte[0];
        if(!StringUtils.isBlank(imgdata)){
            imgData = Base64.decodeBase64(imgdata);
//            template.opsForList().leftPush(key, imgdata);
        }
        return imgData;
    }
}
