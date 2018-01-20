package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.DeviceAndroid;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface DeviceMapper extends BaseMapper<Device, Long> {

	long insertAndroid(DeviceAndroid entity);

    void updateAndroid(DeviceAndroid entity);

    void updateStatus(@Param("deviceId") String deviceId, @Param("status") DB.PhoneOnlineStatus status);

    List<Device> findAll(@Param("deviceIds") List<String> deviceIds);
    Device findOne(@Param("deviceId")String deviceId);

    List<Device> fetchList(Map queryMap);

    List<DeviceAndroid> findAllDeviceAndroid(@Param("deviceIds") List<String> deviceIds);

    List<Device> findOnlineList(Map queryMap);

    List<Device> findListByOnlineDevice(@Param("query") Map<String, Object> queryMap, @Param("onlineDeviceList") Set<String> onlineDeviceList);
}