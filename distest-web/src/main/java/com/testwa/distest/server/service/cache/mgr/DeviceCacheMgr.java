package com.testwa.distest.server.service.cache.mgr;

import com.testwa.distest.redis.RedisCacheManager;
import com.testwa.distest.server.entity.DeviceBase;
import com.testwa.distest.server.service.cache.constant.DeviceConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Created by wen on 20/10/2017.
 */
@Component
public class DeviceCacheMgr {

    @Autowired
    private RedisCacheManager redisCacheMgr;

    public void saveDeviceContent(DeviceBase device){
        redisCacheMgr.put(DeviceConstant.DEVICE_ANDROID_KEY + device.getDeviceId(), device);
    }

    public void delDeviceContent(String deviceId){
        redisCacheMgr.remove(DeviceConstant.DEVICE_ANDROID_KEY + deviceId);
    }

    public DeviceBase getDeviceContent(String deviceId){
        return (DeviceBase) redisCacheMgr.get(DeviceConstant.DEVICE_ANDROID_KEY + deviceId);
    }

}
