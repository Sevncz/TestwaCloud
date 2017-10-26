package com.testwa.distest.server.web.device.cache;

import com.testwa.distest.redis.RedisCacheManager;
import com.testwa.distest.server.mvc.entity.DeviceAndroid;
import com.testwa.distest.server.mvc.entity.DeviceBase;
import com.testwa.distest.server.mvc.entity.DeviceIOS;
import com.testwa.distest.server.web.device.constant.DeviceConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Created by wen on 20/10/2017.
 */
@Component
public class DeviceCacheMgr {

    @Autowired
    private RedisCacheManager redisCacheMgr;

    public void saveDevice(DeviceBase device){
        redisCacheMgr.put(DeviceConstant.DEVICE_ANDROID_KEY + device.getDeviceId(), device);
    }

    public void delDevice(String deviceId){
        redisCacheMgr.remove(DeviceConstant.DEVICE_ANDROID_KEY + deviceId);
    }

    public DeviceBase getDevice(String deviceId){
        return (DeviceBase) redisCacheMgr.get(DeviceConstant.DEVICE_ANDROID_KEY + deviceId);
    }

}
