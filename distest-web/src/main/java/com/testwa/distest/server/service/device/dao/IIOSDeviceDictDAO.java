package com.testwa.distest.server.service.device.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.IOSDeviceDict;


public interface IIOSDeviceDictDAO extends IBaseDAO<IOSDeviceDict, Long> {
    IOSDeviceDict findByProductType(String productType);
}
