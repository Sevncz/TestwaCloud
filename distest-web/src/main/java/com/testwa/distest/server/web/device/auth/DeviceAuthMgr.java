package com.testwa.distest.server.web.device.auth;

import com.testwa.core.redis.RedisCacheManager;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.service.device.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DeviceAuthMgr {

    private static final String ALLOW_USER_TO_USE = "devices.allowUser.{0}";
    private static final String ALLOW_USER_TO_USE_VALUE = "devices.allowUser.value";
    private static final String ALLOW_PROJECT_TO_USE = "devices.allowProject.{0}";
    private static final String ALLOW_PROJECT_TO_USE_VALUE = "devices.allowProject.value";
    private static final String USING = "devices.using";
    private static final String ONLINE = "devices.online";

    @Autowired
    private RedisCacheManager redisCacheMgr;
    @Autowired
    private DeviceService deviceService;

    /**
     * 分享给该用户的设备列表
     * @param deviceId
     * @param userId
     */
    public void allowUserToUse(String deviceId, Long userId){
        String key = MessageFormat.format(ALLOW_USER_TO_USE, userId);
        redisCacheMgr.sAdd(key, deviceId);
        redisCacheMgr.sAdd(ALLOW_USER_TO_USE_VALUE, String.valueOf(userId));
    }
    public void allowUsersToUse(String deviceId, List<Long> userIds){
        for (Long userId: userIds) {
            String key = MessageFormat.format(ALLOW_USER_TO_USE, userId);
            redisCacheMgr.sAdd(key, deviceId);
            redisCacheMgr.sAdd(ALLOW_USER_TO_USE_VALUE, String.valueOf(userId));
        }
    }
    public void disableUserToUse(String deviceId, Long userId){
        String key = MessageFormat.format(ALLOW_USER_TO_USE, userId);
        redisCacheMgr.sRem(key, deviceId);
    }
    public Set<String> getAllDeviceIdsByUser(Long userId){
        String key = MessageFormat.format(ALLOW_USER_TO_USE, userId);
        return getOnlyOnlineDeviceId(key);
    }


    /**
     * 分享给该项目的设备列表
     * @param deviceId
     * @param proejctId
     */
//    public void allowProjectToUse(String deviceId, Long proejctId){
//        String key = MessageFormat.format(ALLOW_PROJECT_TO_USE, proejctId);
//        redisCacheMgr.sAdd(key, deviceId);
//        redisCacheMgr.sAdd(ALLOW_PROJECT_TO_USE_VALUE, String.valueOf(proejctId));
//    }
//    public void disableProjectToUse(String deviceId, Long proejctId){
//        String key = MessageFormat.format(ALLOW_PROJECT_TO_USE, proejctId);
//        redisCacheMgr.sRem(key, deviceId);
//
//    }
//    public Set<String> getAllDeviceIdsByProject(Long proejctId){
//        String key = MessageFormat.format(ALLOW_PROJECT_TO_USE, proejctId);
//        return getOnlyOnlineDeviceId(key);
//    }

    /**
     * 分享范围的设备和在线设备的交集
     * @param key
     * @return
     */
    private Set<String> getOnlyOnlineDeviceId(String key) {
        Set<String> allDevices = redisCacheMgr.sMembers(key);
        Set<String> onlineDevices = allOnlineDevices();
        onlineDevices.retainAll(allDevices);
        return onlineDevices;
    }

    /**
     * 使用中的设备列表
     * @param deviceId
     */
    public void using(String deviceId){
        redisCacheMgr.sAdd(USING, deviceId);
    }
    public Set<String> allUsing(){
        return getOnlyOnlineDeviceId(USING);
    }
    public Boolean isUsing(String deviceId){
        return redisCacheMgr.sIsMember(USING, deviceId);
    }
    public void releaseDev(String deviceId) {
        redisCacheMgr.sRem(USING, deviceId);
    }

    /**
     * 保存在线设备Id
     * @param deviceId
     */
    public void online(String deviceId) {
        redisCacheMgr.sAdd(ONLINE, deviceId);
//        deviceService.updateStatus(deviceId, DB.PhoneOnlineStatus.ONLINE);
    }
    public void offline(String deviceId) {
        redisCacheMgr.sRem(ONLINE, deviceId);
//        deviceService.updateStatus(deviceId, DB.PhoneOnlineStatus.OFFLINE);
    }
    public Set<String> allOnlineDevices() {
        return redisCacheMgr.sMembers(ONLINE);
    }

    public Set<String> allEnableDevices() {
        return redisCacheMgr.sMembers(ONLINE);
    }

    /**
     * 清理 ALLOW_USER_TO_USE， ALLOW_PROJECT_TO_USE， USING 中不在线的设备
     */
    public void mergeOnline() {
        Set<String> userIds = redisCacheMgr.sMembers(ALLOW_USER_TO_USE_VALUE);
        for(String userId : userIds){
            String key = MessageFormat.format(ALLOW_USER_TO_USE, userId);
            redisCacheMgr.sInterStore(key, key, ONLINE);
        }
        Set<String> projectIds = redisCacheMgr.sMembers(ALLOW_PROJECT_TO_USE_VALUE);
        for(String projectId : projectIds){
            String key = MessageFormat.format(ALLOW_PROJECT_TO_USE, projectId);
            redisCacheMgr.sInterStore(key, key, ONLINE);
        }

        redisCacheMgr.sInterStore(USING, USING, ONLINE);
    }

}
