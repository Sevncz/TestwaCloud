package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.DeviceSharer;
import com.testwa.distest.server.service.device.dto.DeviceSharerDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DeviceSharerMapper extends BaseMapper<DeviceSharer, Long> {

    DeviceSharer findShareUserIn(@Param("deviceId") String deviceId, @Param("ownerId") Long ownerId, @Param("toUserId") Long toUserId);

    List<DeviceSharer> findShareToUserList(@Param("inDeviceList") Set<String> inDeviceList, @Param("toUserId") Long toUserId);

    List<DeviceSharerDTO> findDeviceScopeUserList(@Param("deviceId") String deviceId, @Param("ownerId") Long ownerId);

    void removeOne(@Param("deviceId") String deviceId, @Param("shareId") Long shareId, @Param("ownerId") Long ownerId);
}
