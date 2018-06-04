package com.testwa.distest.server.service.device.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.service.device.dto.DeviceOneCategoryResultDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IDeviceDAO extends IBaseDAO<Device, Long> {
//    List<Device> findBy(Device entity);

    long insertAndroid(Device entity);

    void updateAndroid(Device entity);

    List<Device> findOnlineList(Map<String, Object> queryMap);

    void updateOnlineStatus(String deviceId, DB.PhoneOnlineStatus status);

    void updateWorkStatus(String deviceId, DB.DeviceWorkStatus status);

    List<Device> findAll(List<String> deviceIds);
    
    Device findOne(String deviceId);

    List<Device> fetchList(Map<String, Object> queryMap);

    List<Device> findListByOnlineDevice(Map<String, Object> queryMap, Set<String> onlineDeviceList);

    Long countBy(Device dq);

    List<DeviceOneCategoryResultDTO> getResolutionCategory(Set<String> deviceIds);

    List<DeviceOneCategoryResultDTO> getOSVersionCategory(Set<String> deviceIds);

    List<DeviceOneCategoryResultDTO> getBrandCategory(Set<String> deviceIds);

    void updateDebugStatus(String deviceId, DB.DeviceDebugStatus status);
}
