package com.testwa.distest.server.web.device.validator;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.exception.DeviceException;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.web.device.mgr.DeviceOnlineMgr;
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
    private DeviceOnlineMgr deviceOnlineMgr;
    @Autowired
    private DeviceService deviceService;

    public void validateOnline(List<String> deviceIds) {
        Set<String> onlineDeviceIdList = deviceOnlineMgr.allOnlineDevices();
        for(String d : deviceIds){

            if(!onlineDeviceIdList.contains(d)){
                throw new DeviceException(ResultCode.CONFLICT, "设备不在线");
            }
        }
    }
    public void validateOnline(String deviceId) {
        Set<String> onlineDeviceIdList = deviceOnlineMgr.allOnlineDevices();
        if(!onlineDeviceIdList.contains(deviceId)){
            throw new DeviceException(ResultCode.CONFLICT, "设备不在线");
        }
    }

    public void validateDeviceExist(List<String> deviceIds) {
        List<Device> projectList = deviceService.findAll(deviceIds);
        if(projectList == null || projectList.size() != deviceIds.size()){
            throw new DeviceException(ResultCode.NOT_FOUND, "设备不存在");
        }
    }

    public Device validateDeviceExist(String deviceId) {
        Device entity = deviceService.getByDeviceId(deviceId);
        if(entity == null){
            throw new DeviceException(ResultCode.NOT_FOUND, "设备不存在");
        }
        return entity;
    }

    public void validateDeviceBelongUser(String deviceId, Long userId) {
        Device entity = deviceService.getByDeviceId(deviceId);
        if(!entity.getLastUserId().equals(userId)){
            throw new DeviceException(ResultCode.CONFLICT, "该设备不属于用户");
        }
    }

    /**
     *@Description: 检查设备是否可用，所有状态为free
     *@Param: [deviceIds]
     *@Return: void
     *@Author: wen
     *@Date: 2018/5/29
     */
    public void validateUsable(List<String> deviceIds) {
        Set<String> onlineDeviceIdList = deviceOnlineMgr.allOnlineDevices();
        List<Device> deviceList = deviceService.findAll(deviceIds);
        for(Device device : deviceList) {
            if(!onlineDeviceIdList.contains(device.getDeviceId())){
                throw new DeviceException(ResultCode.CONFLICT, "设备不在线");
            } else if (!DB.DeviceWorkStatus.FREE.equals(device.getWorkStatus()) || !DB.DeviceDebugStatus.FREE.equals(device.getDebugStatus())) {
                throw new DeviceException(ResultCode.CONFLICT, "设备忙碌中");
            }
        }

    }

    public void validateUsable(String deviceId) {
        Set<String> onlineDeviceIdList = deviceOnlineMgr.allOnlineDevices();
        if(!onlineDeviceIdList.contains(deviceId)){
            throw new DeviceException(ResultCode.CONFLICT, "设备不在线");
        } else {
            Device device = deviceService.findByDeviceId(deviceId);
            if(device == null) {
                throw new DeviceException(ResultCode.NOT_FOUND, "该设备不存在");
            }
            if (!DB.DeviceWorkStatus.FREE.equals(device.getWorkStatus()) || !DB.DeviceDebugStatus.FREE.equals(device.getDebugStatus())) {
                throw new DeviceException(ResultCode.CONFLICT, "设备忙碌中");
            }
        }
    }
}
