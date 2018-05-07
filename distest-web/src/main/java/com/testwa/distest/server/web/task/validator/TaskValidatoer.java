package com.testwa.distest.server.web.task.validator;

import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.exception.TaskStartException;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.task.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Component
public class TaskValidatoer {

    @Autowired
    private TaskService executionTaskService;
    @Autowired
    private AppService appService;
    @Autowired
    private DeviceService deviceService;


    public Task validateTaskExist(Long entityId) throws ObjectNotExistsException {
        Task exetask = executionTaskService.findOne(entityId);
        if(exetask == null){
            throw new ObjectNotExistsException("任务不存在");
        }
        return exetask;
    }


    public List<Task> validateTasksExist(List<Long> entityIds) throws ObjectNotExistsException {
        List<Task> entityList = executionTaskService.findAll(entityIds);
        if(entityList == null || entityList.size() != entityIds.size()){
            throw new ObjectNotExistsException("任务不存在");
        }
        return entityList;
    }


    public void validateAppAndDevicePlatform(Long appId, List<String> deviceIds) throws TaskStartException {
        App app = appService.findOne(appId);
        DB.PhoneOS platform = app.getPlatform();
        List<Device> deviceList = deviceService.findAll(deviceIds);
        for(Device device : deviceList) {
            if(device.getPhoneOS() != null && !device.getPhoneOS().equals(platform)){
                throw new TaskStartException("App和设备系统不匹配");
            }
        }

    }
}
