package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.DeviceBase;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceBaseMapper extends BaseMapper<DeviceBase, Long> {

	List<DeviceBase> findBy(DeviceBase entity);

}