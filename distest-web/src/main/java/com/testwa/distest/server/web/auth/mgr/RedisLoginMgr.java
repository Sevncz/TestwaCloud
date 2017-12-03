package com.testwa.distest.server.web.auth.mgr;

import com.testwa.core.redis.RedisCacheManager;
import com.testwa.distest.server.service.user.constant.UserConstant;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Created by wen on 20/10/2017.
 */
@Log4j2
@Component
public class RedisLoginMgr {

    @Autowired
    private RedisCacheManager redisCacheMgr;

    private String getRedisKey(String username){
        return UserConstant.USER_KEY + username;
    }

    public void login(String username,  String accessToken){
        redisCacheMgr.put(getRedisKey(username), accessToken);
    }

    public void logout(String username) {
        redisCacheMgr.remove(getRedisKey(username));
    }

    private static int captchaExpires = 3*60; //超时时间3min

    public void loginCaptcha(String uuid, String answer) {
        redisCacheMgr.put(uuid, captchaExpires, answer);
    }
}