package com.testwa.distest.server.service.device.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.DeviceAndroid;
import com.testwa.distest.server.mapper.DeviceMapper;
import com.testwa.distest.server.service.device.dao.IDeviceDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class DeviceDAO extends BaseDAO<Device, Long>  implements IDeviceDAO{

    @Resource
    private DeviceMapper deviceMapper;


    @Override
    public long insertAndroid(DeviceAndroid entity) {
        return deviceMapper.insertAndroid(entity);
    }

    @Override
    public void updateAndroid(DeviceAndroid entity) {
        deviceMapper.updateAndroid(entity);
    }

    @Override
    public void updateStatus(String deviceId, DB.PhoneOnlineStatus status) {
        deviceMapper.updateStatus(deviceId, status);
    }

    @Override
    public List<Device> findAll(List<String> deviceIds) {
        return deviceMapper.findAll(deviceIds);
    }

    @Override
    public Device findOne(String deviceId) {
        return deviceMapper.findOne(deviceId);
    }

    @Override
    public List<Device> fetchList(Map<String, Object> queryMap) {
        return deviceMapper.fetchList(queryMap);
    }

    @Override
    public List<DeviceAndroid> findAllDeviceAndroid(List<String> deviceIds) {
        return deviceMapper.findAllDeviceAndroid(deviceIds);
    }

    @Override
    public List<Device> findOnlineList(Map queryMap) {
        return deviceMapper.findOnlineList(queryMap);
    }

    @Override
    public List<Device> findListByOnlineDevice(Map<String, Object> queryMap, Set<String> onlineDeviceList) {
        return deviceMapper.findListByOnlineDevice(queryMap, onlineDeviceList);
    }
}
