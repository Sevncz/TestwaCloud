package com.testwa.distest.server.web.device.validator;

import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.Project;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Created by wen on 24/10/2017.
 */
@Component
public class DeviceValidatoer {

    @Autowired
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private DeviceService deviceService;

    public void validateOnline(List<String> deviceIds) throws ObjectNotExistsException {
        Set<String> onlineDeviceIdList = deviceAuthMgr.allOnlineDevices();
        for(String d : deviceIds){

            if(!onlineDeviceIdList.contains(d)){
                throw new ObjectNotExistsException("设备不在线");
            }
        }
    }
    public void validateOnline(String deviceId) throws ObjectNotExistsException {
        Set<String> onlineDeviceIdList = deviceAuthMgr.allOnlineDevices();
        if(!onlineDeviceIdList.contains(deviceId)){
            throw new ObjectNotExistsException("设备不在线");
        }
    }

    public void validateDeviceExist(List<String> deviceIds) throws ObjectNotExistsException {
        List<Device> projectList = deviceService.findAll(deviceIds);
        if(projectList == null || projectList.size() != deviceIds.size()){
            throw new ObjectNotExistsException("设备不存在");
        }
    }

    public Device validateDeviceExist(String deviceId) throws ObjectNotExistsException {
        Device entity = deviceService.findOne(deviceId);
        if(entity == null){
            throw new ObjectNotExistsException("设备不存在");
        }
        return entity;
    }
}
