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

    private static final String ONLINE = "devices.online";

    @Autowired
    private RedisCacheManager redisCacheMgr;
    @Autowired
    private DeviceService deviceService;

    /**
     * 保存在线设备Id
     * @param deviceId
     */
    public void online(String deviceId) {
        redisCacheMgr.sAdd(ONLINE, deviceId);
        deviceService.updateStatus(deviceId, DB.PhoneOnlineStatus.ONLINE);
    }
    public void offline(String deviceId) {
        redisCacheMgr.sRem(ONLINE, deviceId);
        deviceService.updateStatus(deviceId, DB.PhoneOnlineStatus.OFFLINE);
    }
    public Set<String> allOnlineDevices() {
        return redisCacheMgr.sMembers(ONLINE);
    }

    public boolean isOnline(String deviceId) {
        return redisCacheMgr.sIsMember(ONLINE, deviceId);
    }

    public void delAllOnline() {
        redisCacheMgr.remove(ONLINE);
    }

}
