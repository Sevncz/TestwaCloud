package com.testwa.distest.server.service.cache.mgr;

import com.testwa.distest.redis.RedisCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DeviceSessionMgr {

    private static final String device_client_login = "device.client.session.%s";

    @Autowired
    private RedisCacheManager redisCacheMgr;

    public void login(String deviceId, String sessionId){

        redisCacheMgr.put(String.format(device_client_login, deviceId), sessionId);

    }

    public void logout(String deviceId){

        redisCacheMgr.remove(String.format(device_client_login, deviceId));

    }

    public String getClientSessionId(String deviceId){

        return (String) redisCacheMgr.get(String.format(device_client_login, deviceId));

    }

}
