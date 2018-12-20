package com.testwa.distest.server.service.device.service;

import com.testwa.core.base.service.BaseService;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.DeviceShareScope;
import com.testwa.distest.server.mapper.DeviceShareScopeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class DeviceShareScopeService extends BaseService<DeviceShareScope, Long> {

    @Autowired
    private DeviceShareScopeMapper deviceShareScopeMapper;

    public DeviceShareScope findOneByDeviceIdAndCreateBy(String deviceId, Long createBy) {
        return deviceShareScopeMapper.findOneByDeviceIdAndCreateBy(deviceId, createBy);
    }

    /**
     * @Description: 更新设备分享范围
     * @Param: [deviceId, createBy, scopeEnum]
     * @Return: void
     * @Author wen
     * @Date 2018/10/29 19:05
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateScope(String deviceId, Long createBy, DB.DeviceShareScopeEnum scopeEnum) {
        deviceShareScopeMapper.updateScope(deviceId, createBy, scopeEnum);
    }

    /**
     * @Description: 保存或者更新，如果不存在，则保存，存在，则更新范围
     * @Param: [deviceId, userId, deviceShareScopeEnum]
     * @Return: void
     * @Author wen
     * @Date 2018/10/29 19:06
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateOrSave(String deviceId, Long userId, Integer scopeId) {
        DB.DeviceShareScopeEnum deviceShareScopeEnum = DB.DeviceShareScopeEnum.fromValue(scopeId);

        DeviceShareScope scope = deviceShareScopeMapper.findOneByDeviceIdAndCreateBy(deviceId, userId);
        if(scope == null) {
            DeviceShareScope ds = new DeviceShareScope();
            ds.setCreateBy(userId);
            ds.setCreateTime(new Date());
            ds.setDeviceId(deviceId);
            ds.setShareScope(deviceShareScopeEnum);
            deviceShareScopeMapper.insert(ds);
        }else{
            if(!deviceShareScopeEnum.equals(scope.getShareScope())) {
                deviceShareScopeMapper.updateScope(deviceId, userId, deviceShareScopeEnum);
            }
        }

    }

}
