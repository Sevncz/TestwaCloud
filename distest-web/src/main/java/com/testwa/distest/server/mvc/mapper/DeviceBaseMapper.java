package com.testwa.distest.server.mvc.mapper;

import com.testwa.distest.common.mapper.BaseMapper;
import com.testwa.core.entity.App;
import com.testwa.core.entity.DeviceBase;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DeviceBaseMapper extends BaseMapper<DeviceBase, Long> {

	List<DeviceBase> findBy(DeviceBase entity);

}