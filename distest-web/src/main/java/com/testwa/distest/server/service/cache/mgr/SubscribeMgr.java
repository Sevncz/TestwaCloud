package com.testwa.distest.server.service.cache.mgr;

import com.testwa.distest.redis.RedisCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SubscribeMgr {

    static final String subscribe_device_func = "subscribe.device.func.%s.%s";

    @Autowired
    private RedisCacheManager redisCacheMgr;

    public void subscribeDeviceEvent(String deviceId, String func, String sessionId) {
        redisCacheMgr.hput(String.format(subscribe_device_func, deviceId, func), sessionId, "000000");
    }

    public Set<String> getSubscribes(String deviceId, String func) {
        return redisCacheMgr.hKeys(String.format(subscribe_device_func, deviceId, func));
    }

    public void delSubscribes(String deviceId, String func) {
        redisCacheMgr.remove(String.format(subscribe_device_func, deviceId, func));
    }

    public void delSubscribe(String deviceId, String func, String sessionId) {
        redisCacheMgr.hdel(String.format(subscribe_device_func, deviceId, func), sessionId);
    }

}
