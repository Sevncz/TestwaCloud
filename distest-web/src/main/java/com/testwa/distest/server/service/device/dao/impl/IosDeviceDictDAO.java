package com.testwa.distest.server.service.device.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.server.entity.IosDeviceDict;
import com.testwa.distest.server.mapper.IosDeviceDictMapper;
import com.testwa.distest.server.service.device.dao.IIosDeviceDictDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class IosDeviceDictDAO extends BaseDAO<IosDeviceDict, Long>  implements IIosDeviceDictDAO {

    @Resource
    private IosDeviceDictMapper mapper;

    @Override
    public IosDeviceDict findByProductType(String productType) {
        return mapper.findByProductType(productType);
    }
}
