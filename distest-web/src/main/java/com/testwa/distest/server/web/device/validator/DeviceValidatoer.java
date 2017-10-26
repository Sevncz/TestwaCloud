package com.testwa.distest.server.web.device.validator;

import com.testwa.distest.common.exception.ObjectNotExistsException;
import com.testwa.distest.server.mvc.entity.DeviceBase;
import com.testwa.distest.server.web.device.cache.DeviceCacheMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Component
public class DeviceValidatoer {

    @Autowired
    private DeviceCacheMgr deviceCacheMgr;

    public List<DeviceBase> validateOnline(List<String> deviceIds) throws ObjectNotExistsException {
        List<DeviceBase> deviceList = new ArrayList<>();
        for(String d : deviceIds){

            DeviceBase deviceBase = deviceCacheMgr.getDevice(d);
            if(deviceBase != null){
                throw new ObjectNotExistsException("设备不在线");
            }
            deviceList.add(deviceBase);
        }
        return deviceList;
    }
}
