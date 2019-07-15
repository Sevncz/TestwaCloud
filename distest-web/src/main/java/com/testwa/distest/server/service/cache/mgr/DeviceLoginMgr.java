package com.testwa.distest.server.service.cache.mgr;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class DeviceLoginMgr {

    // client 中 remoteClient 连接服务器
    private static final String ws_device_remoteClient = "ws.device.remoteClient.%s";
    private static final String ws_device_remoteClient_pattern = "ws.device.remoteClient.*";

    @Resource
    private RedissonClient redissonClient;

    private String getKey(String deviceId) {
        return String.format(ws_device_remoteClient, deviceId);
    }

    public void login(String deviceId, String sessionId){
        log.info("device [{}] login ... ...", deviceId);
        RBucket<String> bucket = redissonClient.getBucket(getKey(deviceId));
        bucket.setAsync(sessionId);
    }

    public void logout(String deviceId){
        RBucket<String> bucket = redissonClient.getBucket(getKey(deviceId));
        bucket.delete();

    }

    public String getDeviceSession(String deviceId){
        RBucket<String> bucket = redissonClient.getBucket(getKey(deviceId));
        return bucket.get();

    }

    public void delAllDeviceSessions() {
        RKeys keys = redissonClient.getKeys();
        Iterable<String> matches = keys.getKeysByPattern(ws_device_remoteClient_pattern);
        for (String match : matches) {
            RBucket<List<String>> bucket = redissonClient.getBucket(match);
            if (bucket != null) {
                bucket.delete();
            }
        }
    }
}
