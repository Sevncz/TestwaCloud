package com.testwa.distest.server.service.device.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.IosDeviceDict;
import com.testwa.distest.server.mapper.DeviceMapper;
import com.testwa.distest.server.mapper.IosDeviceDictMapper;
import com.testwa.distest.server.service.device.dao.IDeviceDAO;
import com.testwa.distest.server.service.device.dao.IIosDeviceDictDAO;
import com.testwa.distest.server.service.device.dto.DeviceOneCategoryResultDTO;
import com.testwa.distest.server.service.device.dto.PrivateDeviceDTO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class IosDeviceDictDAO extends BaseDAO<IosDeviceDict, Long>  implements IIosDeviceDictDAO {

    @Resource
    private IosDeviceDictMapper mapper;


    @Override
    public IosDeviceDict findByProductType(String productType) {
        return mapper.findByProductType(productType);
    }
}
