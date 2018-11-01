package com.testwa.distest.server.service.device.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.DeviceSharer;
import com.testwa.distest.server.service.device.dto.DeviceSharerDTO;

import java.util.List;
import java.util.Set;

public interface IDeviceSharerDAO extends IBaseDAO<DeviceSharer, Long> {
    DeviceSharer findShareUserIn(String deviceId, Long ownerId, Long toUserId);

    List<DeviceSharer> findShareToUserList(Set<String> inDeviceList, Long userId);

    List<DeviceSharerDTO> findDeviceScopeUserList(String deviceId, Long userId);
}
