package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.AgentLoginLog;
import com.testwa.distest.server.entity.DeviceLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceLogMapper extends BaseMapper<DeviceLog, Long> {


}