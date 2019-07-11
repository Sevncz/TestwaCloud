package com.testwa.distest.server.service.cache.mgr;


import com.testwa.core.redis.RedisCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class WebsocketLoginMgr {
    private static final String ws_client = "ws.client.session.%s";
    private static final String ws_client_pattern = "ws.client.session.*";

    @Autowired
    private RedisCacheManager redisCacheMgr;

    private String getKey(Long userId) {
        return String.format(ws_client, userId);
    }

    public void login(Long userId, String sessionId){
        redisCacheMgr.put(getKey(userId), sessionId);
    }

    public void logout(Long userId){
        redisCacheMgr.remove(getKey(userId));
    }

    public String getClientSession(Long userId){
        return (String) redisCacheMgr.get(getKey(userId));
    }
    public void delAllClientSessions(){
        Set<String> keys = redisCacheMgr.keys(ws_client_pattern);
        if(keys != null && !keys.isEmpty()){
            keys.forEach(k -> {
                redisCacheMgr.remove(k);
            });
        }
    }


}
