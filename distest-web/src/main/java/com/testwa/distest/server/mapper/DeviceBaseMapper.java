package com.testwa.distest.server.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.distest.server.entity.DeviceBase;

import java.util.List;

public interface DeviceBaseMapper extends BaseMapper<DeviceBase, Long> {

	List<DeviceBase> findBy(DeviceBase entity);

}