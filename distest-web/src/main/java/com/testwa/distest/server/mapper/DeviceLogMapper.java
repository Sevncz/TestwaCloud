package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.AgentLoginLog;
import com.testwa.distest.server.entity.DeviceLog;
import com.testwa.distest.server.entity.SubTask;
import com.testwa.distest.server.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceLogMapper extends BaseMapper<DeviceLog, Long> {

    DeviceLog findOne(Long key);

    Long sumDebugTime(@Param("members") List<User> members, @Param("projectId") Long projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    Long sumJobTimeByUserCode(@Param("userCode") String userCode, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    Long sumDebugTimeByUserCode(@Param("userCode") String userCode, @Param("startTime") Long startTime, @Param("endTime") Long endTime);
}