package com.testwa.distest.server.service.device.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.DeviceLog;
import com.testwa.distest.server.mapper.DeviceLogMapper;
import com.testwa.distest.server.service.device.dao.IDeviceLogDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class DeviceLogDAO extends BaseDAO<DeviceLog, Long>  implements IDeviceLogDAO {

    @Resource
    private DeviceLogMapper mapper;

}
