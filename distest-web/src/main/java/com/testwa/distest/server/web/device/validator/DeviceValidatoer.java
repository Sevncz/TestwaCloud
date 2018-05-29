package com.testwa.distest.server.web.device.validator;

import com.testwa.core.base.exception.DeviceUnusableException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
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
            throw new DeviceUnusableException("设备不在线");
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

    public void validateDeviceBelongUser(String deviceId, Long userId) throws ObjectNotExistsException {
        Device entity = deviceService.findOne(deviceId);
        if(!entity.getLastUserId().equals(userId)){
            throw new ObjectNotExistsException("该设备不属于用户");
        }
    }

    /**
     *@Description: 检查设备是否可用，所有状态为free
     *@Param: [deviceIds]
     *@Return: void
     *@Author: wen
     *@Date: 2018/5/29
     */
    public void validateUsable(List<String> deviceIds) throws DeviceUnusableException {
        Set<String> onlineDeviceIdList = deviceAuthMgr.allOnlineDevices();
        List<Device> deviceList = deviceService.findAll(deviceIds);
        for(Device device : deviceList) {
            if(!onlineDeviceIdList.contains(device.getDeviceId())){
                throw new DeviceUnusableException("设备不在线");
            } else if (!DB.DeviceWorkStatus.FREE.equals(device.getWorkStatus()) || !DB.DeviceDebugStatus.FREE.equals(device.getDebugStatus())) {
                throw new DeviceUnusableException("设备忙碌中");
            }
        }

    }

    public void validateUsable(String deviceId)  throws DeviceUnusableException, ObjectNotExistsException {
        Set<String> onlineDeviceIdList = deviceAuthMgr.allOnlineDevices();
        if(!onlineDeviceIdList.contains(deviceId)){
            throw new DeviceUnusableException("设备不在线");
        } else {
            Device device = deviceService.findByDeviceId(deviceId);
            if(device == null) {
                throw new ObjectNotExistsException("该设备不存在");
            }
            if (!DB.DeviceWorkStatus.FREE.equals(device.getWorkStatus()) || !DB.DeviceDebugStatus.FREE.equals(device.getDebugStatus())) {
                throw new DeviceUnusableException("设备忙碌中");
            }
        }
    }
}
