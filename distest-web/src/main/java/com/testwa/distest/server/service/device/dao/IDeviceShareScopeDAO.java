package com.testwa.distest.server.service.device.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.DeviceShareScope;

public interface IDeviceShareScopeDAO extends IBaseDAO<DeviceShareScope, Long> {

    void updateScope(String deviceId, Long createBy, DB.DeviceShareScopeEnum aPublic);

    DeviceShareScope findOneByDeviceIdAndCreateBy(String deviceId, Long createBy);
}
