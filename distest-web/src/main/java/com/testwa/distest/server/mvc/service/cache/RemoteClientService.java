package com.testwa.distest.server.mvc.service.cache;

import com.testwa.distest.server.mvc.model.ProjectMember;
import com.testwa.distest.server.mvc.model.TDevice;
import com.testwa.distest.server.mvc.model.UserDeviceHis;
import com.testwa.distest.server.mvc.model.UserShareScope;
import com.testwa.distest.server.mvc.repository.DeviceRepository;
import com.testwa.distest.server.mvc.repository.ProjectMemberRepository;
import com.testwa.distest.server.mvc.repository.UserDeviceHisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
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
    @Autowired
    private UserDeviceHisRepository userDeviceHisRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    public void saveDevice(String userId, String deviceId){
        // user -- device
        redisTemplate.opsForValue().set(String.format(CacheKeys.device_user, deviceId), userId);
        // device -- share scopes
        log.debug("device add share scopes");
        UserDeviceHis userDeviceHis = userDeviceHisRepository.findByUserIdAndDeviceId(userId, deviceId);
        if(userDeviceHis != null ){
            TDevice tDevice = deviceRepository.findOne(userDeviceHis.getDeviceId());
            if(tDevice != null){
                shareDevice(userDeviceHis, tDevice);
            }
        }else{

            log.debug("This device is new");
        }
    }

    private void shareDevice(UserDeviceHis userDeviceHis, TDevice tDevice) {
        if (userDeviceHis.getScope() == UserShareScope.All.getValue()) {
            redisTemplate.opsForValue().set(String.format(CacheKeys.device_share, "all", tDevice.getId()), tDevice);
        } else if (userDeviceHis.getScope() == UserShareScope.Project.getValue()) {
            List<ProjectMember> projectMembers = projectMemberRepository.findByMemberId(userDeviceHis.getUserId());
            projectMembers.forEach(projectMember -> redisTemplate.opsForValue().set(String.format(CacheKeys.device_share, projectMember.getProjectId(), tDevice.getId()), tDevice));
        } else {
            if (userDeviceHis.getScope() == UserShareScope.User.getValue()) {
                // for user list
                userDeviceHis.getShareUsers().forEach(user -> redisTemplate.opsForValue().set(String.format(CacheKeys.device_share, user, tDevice.getId()), tDevice));
            }
            // for owner
            redisTemplate.opsForValue().set(String.format(CacheKeys.device_share, userDeviceHis.getUserId(), tDevice.getId()), tDevice);
        }
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
        // remove share pool, can only loop through all keys
        redisDeleteByPattern(String.format(CacheKeys.device_share, "*", deviceId));
        // remove client
        delDeviceForClient(deviceId);
    }

    private void redisDeleteByPattern(String pattern) {

        redisTemplate.executePipelined((RedisCallback<String>) redisConnection -> {

            StringRedisConnection stringRedisConn = (StringRedisConnection)redisConnection;
            Collection<String> keys = stringRedisConn.keys(pattern);
            stringRedisConn.del(keys.toArray(new String[keys.size()]));
            return null;
        });
    }

    public void delDevice(){
        String parrentKey = String.format(CacheKeys.device_user, "*");
        redisDeleteByPattern(parrentKey);
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

    public List<TDevice> getDeviceByUserIdAndProjectId(String userId, String projectId) {
        Set<String> keys = redisTemplate.keys(String.format(CacheKeys.device_share, "all", "*"));
        keys.addAll(redisTemplate.keys(String.format(CacheKeys.device_share, projectId, "*")));
        keys.addAll(redisTemplate.keys(String.format(CacheKeys.device_share, userId, "*")));

        List<TDevice> deviceList = new ArrayList<>();
        keys.forEach(key -> {
            TDevice tDevice = (TDevice) redisTemplate.opsForValue().get(key);
            deviceList.add(tDevice);
        });
        return deviceList;
    }
}
