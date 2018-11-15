package com.testwa.distest.server.web.task.controller;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.AppiumFile;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.mongo.model.Step;
import com.testwa.distest.server.mongo.service.CrashLogService;
import com.testwa.distest.server.mongo.service.StepService;
import com.testwa.distest.server.service.task.service.AppiumFileService;
import com.testwa.distest.server.service.task.service.SubTaskService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.task.execute.ReportMgr;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api("测试日志")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class LogReportController extends BaseController {

    @Autowired
    private AppiumFileService appiumFileService;
    @Autowired
    private TaskValidatoer taskValidatoer;
    @Autowired
    private CrashLogService crashLogService;
    @Autowired
    private StepService stepService;

    @ApiOperation(value="返回一个appium日志相对路径", notes="")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/device/{deviceId}/appiumLogPath")
    public Result appiumLogPath(@PathVariable Long taskCode,
                                @PathVariable String deviceId) {
        taskValidatoer.validateTaskExist(taskCode);
        AppiumFile appiumFile = appiumFileService.findOne(taskCode, deviceId);
        if(appiumFile == null){
            return Result.error(ResultCode.NOT_FOUND, "Appium 日志不存在");
        }
        return Result.success(appiumFile.buildPath());
    }

    @ApiOperation(value="crash日志")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/device/{deviceId}/crashLog")
    public List crashLog(@PathVariable(value = "taskCode") Long taskCode, @PathVariable(value = "deviceId") String deviceId) {
        taskValidatoer.validateTaskExist(taskCode);
        return crashLogService.findBy(taskCode, deviceId);
    }

    @ApiOperation(value="启动的详细信息")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/device/{deviceId}/launchDetail")
    public Result launchDetail(@PathVariable Long taskCode, @PathVariable String deviceId) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        if(DB.TaskStatus.RUNNING.equals(task.getStatus())) {
            return Result.error(ResultCode.CONFLICT, "任务还未完成");
        }
        Step step = stepService.getLaunchStep(taskCode, deviceId);
        return Result.success(step);
    }
}
