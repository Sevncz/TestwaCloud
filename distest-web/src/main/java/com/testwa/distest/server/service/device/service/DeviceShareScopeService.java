package com.testwa.distest.server.service.device.service;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.DeviceShareScope;
import com.testwa.distest.server.service.device.dao.IDeviceShareScopeDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class DeviceShareScopeService {

    @Autowired
    private IDeviceShareScopeDAO deviceScopeDAO;

    public void insert(DeviceShareScope ds) {
        deviceScopeDAO.insert(ds);
    }

    public DeviceShareScope findOneByDeviceIdAndCreateBy(String deviceId, Long createBy) {
        return deviceScopeDAO.findOneByDeviceIdAndCreateBy(deviceId, createBy);
    }

    /**
     * @Description: 更新设备分享范围
     * @Param: [deviceId, createBy, scopeEnum]
     * @Return: void
     * @Author wen
     * @Date 2018/10/29 19:05
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void updateScope(String deviceId, Long createBy, DB.DeviceShareScopeEnum scopeEnum) {

        deviceScopeDAO.updateScope(deviceId, createBy, scopeEnum);
    }

    /**
     * @Description: 保存或者更新，如果不存在，则保存，存在，则更新范围
     * @Param: [deviceId, userId, deviceShareScopeEnum]
     * @Return: void
     * @Author wen
     * @Date 2018/10/29 19:06
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void updateOrSave(String deviceId, Long userId, Integer scopeId) {
        DB.DeviceShareScopeEnum deviceShareScopeEnum = DB.DeviceShareScopeEnum.fromValue(scopeId);

        DeviceShareScope scope = deviceScopeDAO.findOneByDeviceIdAndCreateBy(deviceId, userId);
        if(scope == null) {
            DeviceShareScope ds = new DeviceShareScope();
            ds.setCreateBy(userId);
            ds.setCreateTime(new Date());
            ds.setDeviceId(deviceId);
            ds.setShareScope(deviceShareScopeEnum);
            deviceScopeDAO.insert(ds);
        }else{
            if(!deviceShareScopeEnum.equals(scope.getShareScope())) {
                deviceScopeDAO.updateScope(deviceId, userId, deviceShareScopeEnum);
            }
        }

    }

}
