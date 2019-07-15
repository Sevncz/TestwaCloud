package com.testwa.distest.server.service.cache.mgr;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class SubscribeDeviceFuncMgr {

    private static final String subscribe_device_func = "subscribe.device.func.%s.%s";
    private static final String subscribe_device_func_partern = "subscribe.device.func.*";

    @Resource
    private RedissonClient redissonClient;

    private String getKey(String deviceId, String func) {
        return String.format(subscribe_device_func, deviceId, func);
    }

    public void subscribeDeviceEvent(String deviceId, String func, String sessionId) {
        RSet<String> rSet = redissonClient.getSet(getKey(deviceId, func));
        rSet.add(sessionId);
    }

    public Set<String> getSubscribes(String deviceId, String func) {
        RSet<String> rSet = redissonClient.getSet(getKey(deviceId, func));
        return rSet.readAll();
    }

    public Boolean isSubscribes(String deviceId, String func, String sessionId) {
        RSet<String> rSet = redissonClient.getSet(getKey(deviceId, func));
        return rSet.contains(sessionId);
    }

    public void delAllSubscribes() {
        RKeys keys = redissonClient.getKeys();
        Iterable<String> matches = keys.getKeysByPattern(subscribe_device_func_partern);
        for (String match : matches) {
            RBucket<List<String>> bucket = redissonClient.getBucket(match);
            if (bucket != null) {
                bucket.delete();
            }
        }
    }

    public void delSubscribe(String deviceId, String func, String sessionId) {
        RSet<String> rSet = redissonClient.getSet(getKey(deviceId, func));
        rSet.remove(sessionId);
    }
}
