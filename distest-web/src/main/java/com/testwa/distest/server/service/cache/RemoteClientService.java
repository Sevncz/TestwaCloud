package com.testwa.distest.server.service.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;


/**
 * Created by wen on 24/06/2017.
 */
@Service
public class RemoteClientService {
    private static final Logger log = LoggerFactory.getLogger(RemoteClientService.class);

    @Autowired
    private StringRedisTemplate template;


    public void saveDevice(String userId, String deviceId){
        template.opsForValue().set(String.format(CacheKeys.device_user, deviceId), userId);
    }

    public void delDevice(String deviceId){
        template.delete(String.format(CacheKeys.device_user, deviceId));
    }

    public void delDevice(){
        String parrentKey = String.format(CacheKeys.device_user, "*");
        Set<String> keys = template.keys(parrentKey);
        for (String k : keys){
            template.delete(k);
        }
    }

    public String getMainSessionByDeviceId(String deviceId){

        String userId = template.opsForValue().get(String.format(CacheKeys.device_user, deviceId));
        return getUserLoginClient(userId);
    }

    public void saveDeviceForClient(String session, String deviceId){
        template.opsForValue().set(String.format(CacheKeys.device_client, deviceId), session);
    }

    public String getDeviceForClient(String deviceId){
        return template.opsForValue().get(String.format(CacheKeys.device_client, deviceId));
    }

    public void delDeviceForClient(String deviceId){
        template.delete(String.format(CacheKeys.device_client, deviceId));
    }

    public void delDeviceForClient(){
        String parrentKey = String.format(CacheKeys.device_client, "*");
        Set<String> keys = template.keys(parrentKey);
        for (String k : keys){
            template.delete(k);
        }
    }

    public String getClientSessionByDeviceId(String deviceId){
        return template.opsForValue().get(String.format(CacheKeys.device_client, deviceId));
    }


    public void saveMainInfo(String session, String clientId){
        template.opsForValue().set(String.format(CacheKeys.main_info, session), clientId);
    }

    public void delMainInfo(String session){
        template.delete(String.format(CacheKeys.main_info, session));
    }

    public void delMainInfo(){
        String parrentKey = String.format(CacheKeys.main_info, "*");
        Set<String> keys = template.keys(parrentKey);
        for (String k : keys){
            template.delete(k);
        }
    }
    public String getMainInfoBySession(String session){
        return template.opsForValue().get(String.format(CacheKeys.main_info, session));
    }


    public void userLoginClient(String userId, String session){
        template.opsForValue().set(String.format(CacheKeys.user_client_login, userId), session);
    }
    public void userLogoutClient(String userId){
        template.delete(String.format(CacheKeys.user_client_login, userId));
    }
    public String getUserLoginClient(String userId){
        return template.opsForValue().get(String.format(CacheKeys.user_client_login, userId));
    }


    public void subscribeDeviceEvent(String deviceId, String func, String sessionId){
        template.opsForHash().put(String.format(CacheKeys.subscribe_device_func, deviceId, func), sessionId, "000000");
    }
    public Set<Object> getSubscribes(String deviceId, String func){
        return template.opsForHash().entries(String.format(CacheKeys.subscribe_device_func, deviceId, func)).keySet();
    }
    public void delSubscribes(String deviceId, String func) {
        template.delete(String.format(CacheKeys.subscribe_device_func, deviceId, func));
    }
    public void delSubscribe(String deviceId, String func, String sessionId) {
        template.opsForHash().delete(String.format(CacheKeys.subscribe_device_func, deviceId, func), sessionId);
    }


    public void saveOnstartScreenDevice(String deviceId, String sessionId){
        template.opsForValue().set(String.format(CacheKeys.onstart_screen_device, deviceId), sessionId);
    }
    public Boolean isOnstartScreenDevice(String deviceId){
        Set<String> keys = template.keys(String.format(CacheKeys.onstart_screen_device, deviceId));
        return keys.size() > 0;
    }
    public void delOnstartScreenDevice(String deviceId){
        template.delete(String.format(CacheKeys.onstart_screen_device, deviceId));
    }
    public void delOnstartScreenDevice(){
        String parrentKey = String.format(CacheKeys.onstart_screen_device, "*");
        Set<String> keys = template.keys(parrentKey);
        for (String k : keys){
            template.delete(k);
        }
    }

}
