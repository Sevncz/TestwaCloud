package com.testwa.distest.server.service.device.dao.impl;

import com.testwa.core.base.dao.impl.BaseDAO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.IOSDeviceDict;
import com.testwa.distest.server.mapper.DeviceMapper;
import com.testwa.distest.server.mapper.IOSDeviceDictMapper;
import com.testwa.distest.server.service.device.dao.IDeviceDAO;
import com.testwa.distest.server.service.device.dao.IIOSDeviceDictDAO;
import com.testwa.distest.server.service.device.dto.DeviceOneCategoryResultDTO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class IOSDeviceDictDAO extends BaseDAO<IOSDeviceDict, Long>  implements IIOSDeviceDictDAO {

    @Resource
    private IOSDeviceDictMapper mapper;


    @Override
    public IOSDeviceDict findByProductType(String productType) {
        return mapper.findByProductType(productType);
    }
}
