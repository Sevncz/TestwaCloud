package com.testwa.distest.server.service.device.dao;

import com.testwa.core.base.dao.IBaseDAO;
import com.testwa.distest.server.entity.IosDeviceDict;

public interface IIosDeviceDictDAO extends IBaseDAO<IosDeviceDict, Long> {

    IosDeviceDict findByProductType(String productType);
}
