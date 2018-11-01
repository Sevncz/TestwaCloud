package com.testwa.distest.server.service.device.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.DeviceShareScope;
import com.testwa.distest.server.mapper.DeviceShareScopeMapper;
import com.testwa.distest.server.service.device.dao.IDeviceShareScopeDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class DeviceShareShareScopeDAO extends BaseDAO<DeviceShareScope, Long>  implements IDeviceShareScopeDAO {

    @Resource
    private DeviceShareScopeMapper mapper;

    @Override
    public void updateScope(String deviceId, Long createBy, DB.DeviceShareScopeEnum deviceShareScopeEnum) {
        mapper.updateScope(deviceId, createBy, deviceShareScopeEnum);
    }

    @Override
    public DeviceShareScope findOneByDeviceIdAndCreateBy(String deviceId, Long createBy) {
        return mapper.findOneByDeviceIdAndCreateBy(deviceId, createBy);
    }
}
