package com.testwa.distest.server.web.task.controller;

import com.alibaba.fastjson.JSON;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.exception.TaskStartException;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.service.task.form.*;
import com.testwa.distest.server.web.app.validator.AppValidator;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.task.execute.ExecuteMgr;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.vo.TaskCodeVO;
import com.testwa.distest.server.web.testcase.validator.TestcaseValidatoer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wen on 24/10/2017.
 */
@Slf4j
@Api("任务执行相关api，第二版")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/task")
public class TaskController extends BaseController {
    @Autowired
    private ExecuteMgr executeMgr;
    @Autowired
    private TestcaseValidatoer testcaseValidatoer;
    @Autowired
    private AppValidator appValidator;
    @Autowired
    private DeviceValidatoer deviceValidatoer;
    @Autowired
    private TaskValidatoer taskValidatoer;


    @ApiOperation(value="执行一个回归测试任务")
    @ResponseBody
    @PostMapping(value = "/run")
    public Result run(@RequestBody TaskNewByCaseAndStartForm form) throws ObjectNotExistsException, AuthorizedException, TaskStartException {
        appValidator.validateAppExist(form.getAppId());
        deviceValidatoer.validateUsable(form.getDeviceIds());
        testcaseValidatoer.validateTestcaseExist(form.getTestcaseId());
        taskValidatoer.validateAppAndDevicePlatform(form.getAppId(), form.getDeviceIds());
        Long taskCode = executeMgr.startHG(form);
        return ok(new TaskCodeVO(taskCode));
    }

    @ApiOperation(value="执行一个兼容测试任务")
    @ResponseBody
    @PostMapping(value = "/run/jr")
    public Result runJR(@RequestBody TaskNewStartJRForm form) throws ObjectNotExistsException, AuthorizedException, TaskStartException {
        appValidator.validateAppExist(form.getAppId());
        deviceValidatoer.validateUsable(form.getDeviceIds());
        taskValidatoer.validateAppAndDevicePlatform(form.getAppId(), form.getDeviceIds());
        Long taskCode = executeMgr.startJR(form);
        return ok(new TaskCodeVO(taskCode));
    }


    @ApiOperation(value="停止一个设备任务")
    @ResponseBody
    @PostMapping(value = "/stop")
    public Result stop(@RequestBody TaskStopForm form) throws ObjectNotExistsException {
        Task task = taskValidatoer.validateTaskExist(form.getTaskCode());
        if(form.getDeviceIds() != null && form.getDeviceIds().size() > 0 ){
            List<Device> taskDevices = task.getDevices();
            List<Device> notInTaskDevice = taskDevices.stream().filter(device -> !form.getDeviceIds().contains(device.getDeviceId())).collect(Collectors.toList());
            if (notInTaskDevice != null && notInTaskDevice.size() > 0) {
                throw new ObjectNotExistsException(String.format("设备 %s 不在该任务中", JSON.toJSON(notInTaskDevice)));
            }
            deviceValidatoer.validateOnline(form.getDeviceIds());
        }
        executeMgr.stop(form);
        return ok();
    }

}
