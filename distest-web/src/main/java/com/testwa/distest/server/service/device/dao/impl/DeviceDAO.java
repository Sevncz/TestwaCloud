package com.testwa.distest.server.service.device.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.mapper.DeviceMapper;
import com.testwa.distest.server.service.device.dao.IDeviceDAO;
import com.testwa.distest.server.service.device.dto.DeviceOneCategoryResultDTO;
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
    public long insertAndroid(Device entity) {
        return deviceMapper.insertAndroid(entity);
    }

    @Override
    public void updateAndroid(Device entity) {
        deviceMapper.updateAndroid(entity);
    }

    @Override
    public void updateOnlineStatus(String deviceId, DB.PhoneOnlineStatus status) {
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
    public List<Device> findOnlineList(Map queryMap) {
        return deviceMapper.findOnlineList(queryMap);
    }

    @Override
    public List<Device> findListByOnlineDevice(Map<String, Object> queryMap, Set<String> onlineDeviceList) {
        return deviceMapper.findListByOnlineDevice(queryMap, onlineDeviceList);
    }

    @Override
    public void updateWorkStatus(String deviceId, DB.DeviceWorkStatus status) {
        deviceMapper.updateWorkStatus(deviceId, status);
    }

    @Override
    public Long countBy(Device dq) {
        return deviceMapper.countBy(dq);
    }

    @Override
    public List<DeviceOneCategoryResultDTO> getResolutionCategory(Set<String> deviceIds) {
        return deviceMapper.getResolutionCategory(deviceIds);
    }

    @Override
    public List<DeviceOneCategoryResultDTO> getOSVersionCategory(Set<String> deviceIds) {
        return deviceMapper.getOSVersionCategory(deviceIds);
    }

    @Override
    public List<DeviceOneCategoryResultDTO> getBrandCategory(Set<String> deviceIds) {
        return deviceMapper.getBrandCategory(deviceIds);
    }
}
