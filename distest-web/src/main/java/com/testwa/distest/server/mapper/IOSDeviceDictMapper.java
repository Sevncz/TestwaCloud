package com.testwa.distest.server.mapper;

import com.testwa.core.base.mapper.BaseMapper;
import com.testwa.distest.server.entity.AgentLoginLogger;
import com.testwa.distest.server.entity.IOSDeviceDict;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IOSDeviceDictMapper extends BaseMapper<IOSDeviceDict, Long> {

    IOSDeviceDict findByProductType(@Param("productType") String productType);

}