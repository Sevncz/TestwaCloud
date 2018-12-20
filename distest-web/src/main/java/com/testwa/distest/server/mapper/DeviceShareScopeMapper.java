package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.DeviceShareScope;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceShareScopeMapper extends BaseMapper<DeviceShareScope, Long> {

    void updateScope(@Param("deviceId") String deviceId, @Param("createBy") Long createBy, @Param("scope") DB.DeviceShareScopeEnum deviceShareScopeEnum);

    DeviceShareScope findOneByDeviceIdAndCreateBy(@Param("deviceId") String deviceId, @Param("userId") Long userId);
}
