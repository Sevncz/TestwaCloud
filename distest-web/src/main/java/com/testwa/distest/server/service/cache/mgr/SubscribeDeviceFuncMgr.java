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

    static final String subscribe_device_func = "subscribe.device.func.%s.%s";
    static final String subscribe_device_func_partern = "subscribe.device.func.*";

    @Autowired
    private RedisCacheManager redisCacheMgr;

    private String getKey(String deviceId, String func) {
        return String.format(subscribe_device_func, deviceId, func);
    }

    public void subscribeDeviceEvent(String deviceId, String func, String sessionId) {
        redisCacheMgr.hput(getKey(deviceId, func), sessionId, "000000");
    }

    public Set<String> getSubscribes(String deviceId, String func) {
        return redisCacheMgr.hKeys(getKey(deviceId, func));
    }

    public Boolean isSubscribes(String deviceId, String func, String sessionId) {
        return redisCacheMgr.hExists(getKey(deviceId, func), sessionId);
    }

    public void delAllSubscribes() {

        Set<String> keys = redisCacheMgr.keys(subscribe_device_func_partern);
        keys.forEach(k -> {
            redisCacheMgr.remove(k);
        });
    }

    public void delSubscribe(String deviceId, String func, String sessionId) {
        redisCacheMgr.hdel(getKey(deviceId, func), sessionId);
    }
}