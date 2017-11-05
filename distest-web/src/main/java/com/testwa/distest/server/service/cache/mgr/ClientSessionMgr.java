package com.testwa.distest.server.service.cache.mgr;


import com.testwa.distest.redis.RedisCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ClientSessionMgr {
    private static final String user_client_login = "client.client.session.%s";

    @Autowired
    private RedisCacheManager redisCacheMgr;

    private String getKey(Long userId) {
        return String.format(user_client_login, userId);
    }

    public void login(Long userId, String sessionId){
        redisCacheMgr.put(getKey(userId), sessionId);
    }

    public void logout(Long userId){
        redisCacheMgr.remove(getKey(userId));
    }

    public void getClientSession(Long userId){
        redisCacheMgr.get(getKey(userId));
    }


}
