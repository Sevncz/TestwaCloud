package com.testwa.distest.server.service.device.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.DeviceAuth;
import com.testwa.distest.server.mapper.DeviceAuthMapper;
import com.testwa.distest.server.service.device.dao.IDeviceAuthDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class DeviceAuthDAO extends BaseDAO<DeviceAuth, Long>  implements IDeviceAuthDAO {

    @Resource
    private DeviceAuthMapper deviceAuthMapper;

    @Override
    public List<DeviceAuth> findBy(Map queryMap) {
        return deviceAuthMapper.findBy(queryMap);
    }

    @Override
    public void removeSomeFromDevice(String deviceId, List<Long> userIds, Long createBy) {
        deviceAuthMapper.removeSomeFromDevice(deviceId, userIds, createBy);
    }
}
