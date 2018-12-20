package com.testwa.distest.server.mapper;

import com.testwa.core.base.mybatis.mapper.BaseMapper;
import com.testwa.distest.server.entity.IosDeviceDict;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IosDeviceDictMapper extends BaseMapper<IosDeviceDict, Long> {

    IosDeviceDict findByProductType(@Param("productType") String productType);

}