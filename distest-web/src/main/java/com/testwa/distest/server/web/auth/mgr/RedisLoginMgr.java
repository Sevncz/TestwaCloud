package com.testwa.distest.server.web.auth.mgr;

import com.testwa.core.redis.RedisCacheManager;
import com.testwa.distest.server.service.user.constant.UserConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Component
public class RedisLoginMgr {

    @Autowired
    private RedisCacheManager redisCacheMgr;
    @Value("${jwt.access_token.expiration}")
    private Long access_token_expiration;

    private String getRedisKey(String username){
        return UserConstant.USER_KEY + username;
    }

    /**
     * 保存accessToken，过期时间为token的过期时间
     * @param username
     * @param accessToken
     */
    public void login(String username,  String accessToken){
        redisCacheMgr.put(getRedisKey(username), access_token_expiration.intValue(), accessToken);
    }

    public void logout(String username) {
        redisCacheMgr.remove(getRedisKey(username));
    }

    private static int captchaExpires = 3*60; //超时时间3min

    public void loginCaptcha(String uuid, String answer) {
        redisCacheMgr.put(uuid, captchaExpires, answer);
    }

    public Boolean validateToken(String username, String authToken) {
        String oldToken = (String) redisCacheMgr.get(getRedisKey(username));
        if(StringUtils.isEmpty(oldToken)){
            return true;
        }
        if(oldToken.equals(authToken)){
            return true;
        }
        return false;
    }
}