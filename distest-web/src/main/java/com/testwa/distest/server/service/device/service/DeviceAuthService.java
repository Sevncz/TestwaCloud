package com.testwa.distest.server.service.device.service;

import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.entity.DeviceAuth;
import com.testwa.distest.server.service.device.dao.IDeviceAuthDAO;
import com.testwa.distest.server.service.device.form.DeviceAuthListForm;
import com.testwa.distest.server.service.device.form.DeviceAuthNewForm;
import com.testwa.distest.server.service.device.form.DeviceAuthRemoveForm;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Log4j2
@Service
public class DeviceAuthService {

    @Autowired
    private IDeviceAuthDAO deviceAuthDAO;

    public void insert(DeviceAuthNewForm form, Long createBy){
        for(Long userId : form.getUserIds()){
            DeviceAuth da = new DeviceAuth();
            da.setCreateBy(createBy);
            da.setUserId(userId);
            da.setDeviceId(form.getDeviceId());
            da.setCreateTime(new Date());
            deviceAuthDAO.insert(da);
        }
    }

    public void delete(Long entityId){
        deviceAuthDAO.delete(entityId);
    }
    public void delete(List<Long> entityIds){
        deviceAuthDAO.delete(entityIds);
    }
    public void removeSomeFromDevice(DeviceAuthRemoveForm removeForm, Long createBy){
        deviceAuthDAO.removeSomeFromDevice(removeForm.getDeviceId(), removeForm.getUserIds(), createBy);
    }
}
