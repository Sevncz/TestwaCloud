package com.testwa.distest.server.service.cache.mgr;

import com.testwa.core.redis.RedisCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class DeviceSessionMgr {

    // client 中 remoteClient 连接服务器
    private static final String ws_device_remoteClient = "ws.device.remoteClient.%s";
    private static final String ws_device_remoteClient_pattern = "ws.device.remoteClient.*";

    @Autowired
    private RedisCacheManager redisCacheMgr;

    private String getKey(String deviceId) {
        return String.format(ws_device_remoteClient, deviceId);
    }

    public void login(String deviceId, String sessionId){
        log.info("device [{}] login ... ...", deviceId);
        redisCacheMgr.put(getKey(deviceId), sessionId);
    }

    public void logout(String deviceId){

        redisCacheMgr.remove(getKey(deviceId));

    }

    public String getDeviceSession(String deviceId){

        return (String) redisCacheMgr.get(getKey(deviceId));

    }

    public void delAllDeviceSessions() {
        Set<String> keys = redisCacheMgr.keys(ws_device_remoteClient_pattern);
        keys.forEach(k -> {
            redisCacheMgr.remove(k);
        });
    }
}
