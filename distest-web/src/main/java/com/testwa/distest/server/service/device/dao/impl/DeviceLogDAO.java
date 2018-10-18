package com.testwa.distest.server.service.device.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.DeviceLog;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mapper.DeviceLogMapper;
import com.testwa.distest.server.service.device.dao.IDeviceLogDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class DeviceLogDAO extends BaseDAO<DeviceLog, Long>  implements IDeviceLogDAO {

    @Resource
    private DeviceLogMapper mapper;

    @Override
    public Long sumDebugTime(List<User> members, Long startTime, Long endTime) {
        return mapper.sumDebugTime(members, null, startTime, endTime);
    }

    @Override
    public Long sumDebugTimeByUserCode(String userCode, Long startTime, Long endTime) {
        return mapper.sumDebugTimeByUserCode(userCode, startTime, endTime);
    }

    @Override
    public Long sumJobTimeByUserCode(String userCode, Long startTime, Long endTime) {
        return mapper.sumJobTimeByUserCode(userCode, startTime, endTime);
    }
}
