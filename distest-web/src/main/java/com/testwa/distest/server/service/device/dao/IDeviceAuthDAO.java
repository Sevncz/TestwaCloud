package com.testwa.distest.server.service.device.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.DeviceAuth;

import java.util.List;
import java.util.Map;

public interface IDeviceAuthDAO extends IBaseDAO<DeviceAuth, Long> {

    List<DeviceAuth> findBy(Map queryMap);

    void removeSomeFromDevice(String deviceId, List<Long> userIds, Long createBy);
}
