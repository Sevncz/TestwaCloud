package com.testwa.distest.server.service.cache.mgr;

import com.testwa.core.redis.RedisCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class SubscribeDeviceFuncMgr {

    private static final String subscribe_device_func = "subscribe.device.func.%s.%s";
    private static final String subscribe_device_func_partern = "subscribe.device.func.*";

    @Autowired
    private RedisCacheManager redisCacheMgr;

    private String getKey(String deviceId, String func) {
        return String.format(subscribe_device_func, deviceId, func);
    }

    public void subscribeDeviceEvent(String deviceId, String func, String sessionId) {
        redisCacheMgr.sAdd(getKey(deviceId, func), sessionId);
    }

    public Set<String> getSubscribes(String deviceId, String func) {
        return redisCacheMgr.sMembers(getKey(deviceId, func));
    }

    public Boolean isSubscribes(String deviceId, String func, String sessionId) {
        return redisCacheMgr.sIsMember(getKey(deviceId, func), sessionId);
    }

    public void delAllSubscribes() {

        Set<String> keys = redisCacheMgr.keys(subscribe_device_func_partern);
        if(keys != null && !keys.isEmpty()) {
            keys.forEach(k -> {
                redisCacheMgr.remove(k);
            });
        }
    }

    public void delSubscribe(String deviceId, String func, String sessionId) {
        redisCacheMgr.sRem(getKey(deviceId, func), sessionId);
    }
}
