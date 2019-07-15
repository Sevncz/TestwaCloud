package com.testwa.distest.server.service.cache.mgr;


import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class WebsocketLoginMgr {
    private static final String ws_client = "ws.client.session.%s";
    private static final String ws_client_pattern = "ws.client.session.*";

    @Resource
    private RedissonClient redissonClient;

    private String getKey(Long userId) {
        return String.format(ws_client, userId);
    }

    public void login(Long userId, String sessionId){
        RBucket<String> bucket = redissonClient.getBucket(getKey(userId));
        bucket.set(sessionId);
    }

    public void logout(Long userId){
        RBucket<String> bucket = redissonClient.getBucket(getKey(userId));
        bucket.delete();
    }

    public String getClientSession(Long userId){
        RBucket<String> bucket = redissonClient.getBucket(getKey(userId));
        return bucket.get();
    }

    public void delAllClientSessions(){
        RKeys keys = redissonClient.getKeys();
        Iterable<String> matches = keys.getKeysByPattern(ws_client_pattern);
        for (String match : matches) {
            RBucket<List<String>> bucket = redissonClient.getBucket(match);
            if (bucket != null) {
                bucket.delete();
            }
        }
    }


}
