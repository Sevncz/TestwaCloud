package com.testwa.distest.server.web.task.controller;

import com.testwa.core.base.controller.BaseController;
import com.testwa.core.script.vo.ScriptCaseVO;
import com.testwa.core.script.vo.TaskVO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.script.service.ScriptCaseService;
import com.testwa.distest.server.service.script.service.ScriptMetadataService;
import com.testwa.distest.server.service.task.form.TaskV2StartByScriptsForm;
import com.testwa.distest.server.service.task.service.TaskResultService;
import com.testwa.distest.server.web.app.validator.AppValidator;
import com.testwa.distest.server.web.device.mgr.DeviceLockMgr;
import com.testwa.distest.server.web.device.mgr.DeviceOnlineMgr;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.script.validator.ScriptValidator;
import com.testwa.distest.server.web.task.mgr.ExecuteMgr;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.vo.TaskStartResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Slf4j
@Api(value = "任务执行相关api", tags = "V2.0")
@Validated
@RestController
@RequestMapping("/v2")
public class TaskV2Controller extends BaseController {

    @Autowired
    private ExecuteMgr executeMgr;
    @Autowired
    private ScriptCaseService scriptCaseService;
    @Autowired
    private AppValidator appValidator;
    @Autowired
    private DeviceValidatoer deviceValidatoer;
    @Autowired
    private TaskValidatoer taskValidatoer;
    @Autowired
    private ScriptValidator scriptValidator;
    @Autowired
    private AppService appService;
    @Autowired
    private DeviceOnlineMgr deviceOnlineMgr;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceLockMgr deviceLockMgr;
    @Autowired
    private User currentUser;
    @Value("${lock.work.expire}")
    private Integer workExpireTime;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ScriptMetadataService scriptMetadataService;
    @Autowired
    private TaskResultService taskResultService;

    @ApiOperation(value = "通过脚本运行任务", notes = "")
    @ResponseBody
    @PostMapping(value = "/run/functional/byScript")
    public TaskStartResultVO run(@RequestBody @Valid TaskV2StartByScriptsForm form) {
        appValidator.validateAppExist(form.getAppId());
        appValidator.validateAppInPorject(form.getAppId(), form.getProjectId());
        scriptValidator.validateScriptCaseExist(form.getScriptCaseId());

        taskValidatoer.validateAppAndDevicePlatform(form.getAppId(), form.getDeviceIds());

        App app = appService.get(form.getAppId());

        scriptValidator.validateScriptCaseBelongApp(form.getScriptCaseId(), app.getPackageName());

        Set<String> onlineDeviceIdList = deviceOnlineMgr.allOnlineDevices();
        List<Device> deviceList = deviceService.findAll(form.getDeviceIds());
        List<Device> unableDevices = new ArrayList<>();
        List<String> unableDeviceIds = new ArrayList<>();
        for(Device device : deviceList) {
            if(!onlineDeviceIdList.contains(device.getDeviceId())
                    || !DB.DeviceWorkStatus.FREE.equals(device.getWorkStatus())
                    || !DB.DeviceDebugStatus.FREE.equals(device.getDebugStatus())){
                unableDevices.add(device);
                unableDeviceIds.add(device.getDeviceId());
            }
        }
        List<String> useableList = form.getDeviceIds().stream().filter(item -> !unableDeviceIds.contains(item)).collect(toList());
        TaskStartResultVO vo = new TaskStartResultVO();
        if(useableList.isEmpty()) {
            vo.addUnableDevice(unableDevices);
            return vo;
        }
        for(String deviceId : useableList) {
            deviceLockMgr.workLock(deviceId, currentUser.getUserCode(), workExpireTime);
        }
        ScriptCaseVO scriptCaseDetailVO = scriptCaseService.getScriptCaseDetailVO(form.getScriptCaseId());
        vo = executeMgr.startFunctionalTestTaskV2(useableList, app, scriptCaseDetailVO);
        vo.addUnableDevice(unableDevices);
        return vo;
    }


    @ApiOperation(value = "测试发布任务", notes = "")
    @ResponseBody
    @PostMapping(value = "/testios/push/{deviceId}")
    public String runScript(@PathVariable String deviceId) {

        ScriptCaseVO scriptCaseDetailVO = scriptCaseService.getScriptCaseDetailVO("246380799165857792");
        Map<String, String> map = scriptMetadataService.getPython();
        TaskVO taskVO = new TaskVO();
        taskVO.setScriptCase(scriptCaseDetailVO);
        taskVO.setAppUrl("/Users/wen/dev/TestApp.zip");
        taskVO.setTaskCode(123456L);
        taskVO.setMetadata(map);
        taskVO.setDeviceId(deviceId);

        RTopic topic = redissonClient.getTopic(deviceId);
        topic.publish(taskVO);
        return "成功";
    }

    @ApiOperation(value = "测试发布任务", notes = "")
    @ResponseBody
    @PostMapping(value = "/testandroid/push/{deviceId}")
    public String runAndroidScript(@PathVariable String deviceId) {

        ScriptCaseVO scriptCaseDetailVO = scriptCaseService.getScriptCaseDetailVO("246772097710424064");
        Map<String, String> map = scriptMetadataService.getPython();
        TaskVO taskVO = new TaskVO();
        taskVO.setScriptCase(scriptCaseDetailVO);
        taskVO.setAppUrl("/Users/wen/dev/repositories/appium/sample-code/apps/ApiDemos-debug.apk");
        taskVO.setTaskCode(223456L);
        taskVO.setMetadata(map);
        taskVO.setDeviceId(deviceId);

        RTopic topic = redissonClient.getTopic(deviceId);
        topic.publish(taskVO);
        return "成功";
    }

    @ApiOperation(value = "保存任务执行结果Result", notes = "")
    @ResponseBody
    @PostMapping(value = "/task/result")
    public TaskResult saveTaskResult(@RequestBody @Valid TaskResult result) {
        taskResultService.insert(result);
        return result;
    }
}
