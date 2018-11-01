package com.testwa.distest.server.service.device.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.DeviceSharer;
import com.testwa.distest.server.mapper.DeviceSharerMapper;
import com.testwa.distest.server.service.device.dao.IDeviceSharerDAO;
import com.testwa.distest.server.service.device.dto.DeviceSharerDTO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Repository
public class DeviceSharerDAO extends BaseDAO<DeviceSharer, Long>  implements IDeviceSharerDAO {

    @Resource
    private DeviceSharerMapper mapper;

    @Override
    public DeviceSharer findShareUserIn(String deviceId, Long ownerId, Long toUserId) {
        return mapper.findShareUserIn(deviceId, ownerId, toUserId);
    }

    @Override
    public List<DeviceSharer> findShareToUserList(Set<String> inDeviceList, Long userId) {
        return mapper.findShareToUserList(inDeviceList, userId);
    }

    @Override
    public List<DeviceSharerDTO> findDeviceScopeUserList(String deviceId, Long userId) {
        return mapper.findDeviceScopeUserList(deviceId, userId);
    }
}
