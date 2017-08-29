package com.testwa.distest.server.mvc.service.cache;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Created by wen on 24/06/2017.
 */
@Service
public class RemoteClientService {
    private static final Logger log = LoggerFactory.getLogger(RemoteClientService.class);

    @Autowired
    private RedisTemplate redisTemplate;


    public void saveDevice(String userId, String deviceId){
        redisTemplate.opsForValue().set(String.format(CacheKeys.device_user, deviceId), userId);
    }

    public List<String> getAllDevice(){
        Set<String> keys = redisTemplate.keys(String.format(CacheKeys.device_user, "*"));
        List<String> r = new ArrayList<>();
        keys.forEach(item -> r.add(item.substring("device.user.".length())));
        return r;
    }
    public boolean isOnline(String deviceId){
        return redisTemplate.hasKey(String.format(CacheKeys.device_user, deviceId));
    }

    public void delDevice(String deviceId){
        redisTemplate.delete(String.format(CacheKeys.device_user, deviceId));
    }

    public void delDevice(){
        String parrentKey = String.format(CacheKeys.device_user, "*");
        Set<String> keys = redisTemplate.keys(parrentKey);
        for (String k : keys){
            redisTemplate.delete(k);
        }
    }

    public String getMainSessionByDeviceId(String deviceId){

        String userId = (String) redisTemplate.opsForValue().get(String.format(CacheKeys.device_user, deviceId));
        return getUserLoginClient(userId);
    }

    public void saveDeviceForClient(String session, String deviceId){
        redisTemplate.opsForValue().set(String.format(CacheKeys.device_client, deviceId), session);
    }

    public String getDeviceForClient(String deviceId){
        return (String) redisTemplate.opsForValue().get(String.format(CacheKeys.device_client, deviceId));
    }

    public void delDeviceForClient(String deviceId){
        redisTemplate.delete(String.format(CacheKeys.device_client, deviceId));
    }

    public void delDeviceForClient(){
        String parrentKey = String.format(CacheKeys.device_client, "*");
        Set<String> keys = redisTemplate.keys(parrentKey);
        for (String k : keys){
            redisTemplate.delete(k);
        }
    }

    public String getClientSessionByDeviceId(String deviceId){
        return (String) redisTemplate.opsForValue().get(String.format(CacheKeys.device_client, deviceId));
    }


    public void saveMainInfo(String session, String clientId){
        redisTemplate.opsForValue().set(String.format(CacheKeys.main_info, session), clientId);
    }

    public void delMainInfo(String session){
        redisTemplate.delete(String.format(CacheKeys.main_info, session));
    }

    public void delMainInfo(){
        String parrentKey = String.format(CacheKeys.main_info, "*");
        Set<String> keys = redisTemplate.keys(parrentKey);
        for (String k : keys){
            redisTemplate.delete(k);
        }
    }
    public String getMainInfoBySession(String session){
        return (String) redisTemplate.opsForValue().get(String.format(CacheKeys.main_info, session));
    }


    public void userLoginClient(String userId, String session){
        redisTemplate.opsForValue().set(String.format(CacheKeys.user_client_login, userId), session);
    }
    public void userLogoutClient(String userId){
        redisTemplate.delete(String.format(CacheKeys.user_client_login, userId));
    }
    public String getUserLoginClient(String userId){
        return (String) redisTemplate.opsForValue().get(String.format(CacheKeys.user_client_login, userId));
    }


    public void subscribeDeviceEvent(String deviceId, String func, String sessionId){
        redisTemplate.opsForHash().put(String.format(CacheKeys.subscribe_device_func, deviceId, func), sessionId, "000000");
    }
    public Set<Object> getSubscribes(String deviceId, String func){
        return redisTemplate.opsForHash().entries(String.format(CacheKeys.subscribe_device_func, deviceId, func)).keySet();
    }
    public void delSubscribes(String deviceId, String func) {
        redisTemplate.delete(String.format(CacheKeys.subscribe_device_func, deviceId, func));
    }
    public void delSubscribe(String deviceId, String func, String sessionId) {
        redisTemplate.opsForHash().delete(String.format(CacheKeys.subscribe_device_func, deviceId, func), sessionId);
    }


    public void saveOnstartScreenDevice(String deviceId, String sessionId){
        redisTemplate.opsForValue().set(String.format(CacheKeys.onstart_screen_device, deviceId), sessionId);
    }
    public Boolean isOnstartScreenDevice(String deviceId){
        Set<String> keys = redisTemplate.keys(String.format(CacheKeys.onstart_screen_device, deviceId));
        return keys.size() > 0;
    }
    public void delOnstartScreenDevice(String deviceId){
        redisTemplate.delete(String.format(CacheKeys.onstart_screen_device, deviceId));
    }
    public void delOnstartScreenDevice(){
        String parrentKey = String.format(CacheKeys.onstart_screen_device, "*");
        Set<String> keys = redisTemplate.keys(parrentKey);
        for (String k : keys){
            redisTemplate.delete(k);
        }
    }

}
