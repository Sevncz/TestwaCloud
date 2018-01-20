package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.DeviceAuth;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DeviceAuthMapper extends BaseMapper<DeviceAuth, Long> {

    List<DeviceAuth> findBy(Map queryMap);

    void removeSomeFromDevice(@Param("deviceId") String deviceId, @Param("userIds") List<Long> userIds, Long createBy);

}
