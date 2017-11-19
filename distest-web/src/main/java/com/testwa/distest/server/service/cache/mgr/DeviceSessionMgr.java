package com.testwa.distest.server.service.cache.mgr;

import com.testwa.core.redis.RedisCacheManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Log4j2
@Component
public class DeviceSessionMgr {

    private static final String device_client_login = "device.client.session.%s";

    @Autowired
    private RedisCacheManager redisCacheMgr;

    private String getKey(String deviceId) {
        return String.format(device_client_login, deviceId);
    }

    public void login(String deviceId, String sessionId){
        redisCacheMgr.put(getKey(deviceId), sessionId);
    }

    public void logout(String deviceId){

        redisCacheMgr.remove(getKey(deviceId));

    }

    public String getDeviceSession(String deviceId){

        return (String) redisCacheMgr.get(getKey(deviceId));

    }

}
