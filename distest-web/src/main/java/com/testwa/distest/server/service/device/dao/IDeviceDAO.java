package com.testwa.distest.server.service.device.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.DeviceAndroid;

import java.util.List;
import java.util.Map;

public interface IDeviceDAO extends IBaseDAO<Device, Long> {
//    List<Device> findBy(Device entity);

    long insertAndroid(DeviceAndroid entity);

    void updateAndroid(DeviceAndroid entity);

    List<Device> findBy(Map<String, Object> queryMap);

    void updateStatus(String deviceId, DB.PhoneOnlineStatus status);

    List<Device> findAll(List<String> deviceIds);
    Device findOne(String deviceId);

    List<Device> fetchList(Map<String, Object> queryMap);
}
