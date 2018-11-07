package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.service.device.dto.DeviceOneCategoryResultDTO;
import com.testwa.distest.server.service.device.dto.PrivateDeviceDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface DeviceMapper extends BaseMapper<Device, Long> {

	long insertAndroid(Device entity);

    void updateAndroid(Device entity);

    void updateStatus(@Param("deviceId") String deviceId, @Param("status") DB.PhoneOnlineStatus status);

    List<Device> findAll(@Param("deviceIds") List<String> deviceIds);

    Device findOne(@Param("deviceId")String deviceId);

    List<Device> fetchList(Map queryMap);

    List<Device> findOnlineList(Map queryMap);

    List<Device> findListByOnlineDevice(@Param("query") Map<String, Object> queryMap, @Param("onlineDeviceList") Set<String> onlineDeviceList);

    void updateWorkStatus(@Param("deviceId") String deviceId, @Param("status") DB.DeviceWorkStatus status);

    Long countBy(Device dq);

    List<DeviceOneCategoryResultDTO> getResolutionCategory(@Param("deviceIds") Set<String> deviceIds);

    List<DeviceOneCategoryResultDTO> getBrandCategory(@Param("deviceIds") Set<String> deviceIds);

    List<DeviceOneCategoryResultDTO> getOSVersionCategory(@Param("deviceIds") Set<String> deviceIds);

    void updateDebugStatus(@Param("deviceId")String deviceId, @Param("status") DB.DeviceDebugStatus status);

    List<Device> searchCloudList(Map<String,Object> queryMap);

    List<Device> findAllInWrok();

    List<Device> findOnlineAndPublicDeviceList(Map<String, Object> queryMap);

    List<Device> searchOnlineAndPublicDeviceList(Map<String, Object> queryMap);

    List<PrivateDeviceDTO> findPrivateList(Map<String, Object> queryMap);
    List<PrivateDeviceDTO> searchPrivateList(Map<String, Object> queryMap);
}