package com.testwa.distest.server.web.task.controller;

import com.alibaba.fastjson.JSON;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.*;
import com.testwa.core.base.vo.Result;
import com.testwa.core.tools.SnowflakeIdWorker;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mongo.model.TaskParams;
import com.testwa.distest.server.mongo.service.TaskParamsService;
import com.testwa.distest.server.service.app.service.AppInfoService;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.cache.mgr.DeviceLockCache;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.task.form.*;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.app.validator.AppValidator;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.web.device.mgr.DeviceLockMgr;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.script.validator.ScriptValidator;
import com.testwa.distest.server.web.task.execute.ExecuteMgr;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.vo.TaskStartResultVO;
import com.testwa.distest.server.web.testcase.validator.TestcaseValidatoer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by wen on 24/10/2017.
 */
@Slf4j
@Api("任务执行相关api")
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
    @Autowired
    private ScriptValidator scriptValidator;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private AppService appService;
    @Autowired
    private AppInfoService appInfoService;
    @Value("${lock.work.expire}")
    private Integer workExpireTime;
    @Autowired
    private SnowflakeIdWorker taskIdWorker;
    @Autowired
    private DeviceLockMgr deviceLockMgr;
    @Autowired
    private UserService userService;
    @Autowired
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private TaskParamsService taskParamsService;


    @ApiOperation(value="执行一个回归测试任务")
    @ResponseBody
    @PostMapping(value = "/run")
    public Result run(@RequestBody TaskNewByCaseAndStartForm form) throws ObjectNotExistsException, AuthorizedException, TaskStartException {
        appValidator.validateAppExist(form.getAppId());
        testcaseValidatoer.validateTestcaseExist(form.getTestcaseId());
        taskValidatoer.validateAppAndDevicePlatform(form.getAppId(), form.getDeviceIds());

        User user = userService.findByUsername(WebUtil.getCurrentUsername());

        Set<String> onlineDeviceIdList = deviceAuthMgr.allOnlineDevices();
        List<Device> deviceList = deviceService.findAll(form.getDeviceIds());
        List<Device> unableDevices = new ArrayList<>();
        List<String> unableDeviceIds = new ArrayList<>();
        for(Device device : deviceList) {
            if(!onlineDeviceIdList.contains(device.getDeviceId())){
                unableDevices.add(device);
                unableDeviceIds.add(device.getDeviceId());
            } else if (!DB.DeviceWorkStatus.FREE.equals(device.getWorkStatus()) || !DB.DeviceDebugStatus.FREE.equals(device.getDebugStatus())) {
                unableDevices.add(device);
                unableDeviceIds.add(device.getDeviceId());
            }
        }
        List<String> useableList = form.getDeviceIds().stream().filter(item -> !unableDeviceIds.contains(item)).collect(toList());
        TaskStartResultVO vo = new TaskStartResultVO();
        if(useableList.size() == 0) {
            vo.addUnableDevice(unableDevices);
            return ok(vo);
        }
        for(String deviceId : useableList) {
            deviceLockMgr.workLock(deviceId, user.getUserCode(), workExpireTime);
        }
        Long taskCode = taskIdWorker.nextId();
        App app = appService.findOne(form.getAppId());
        Testcase tc = testcaseService.fetchOne(form.getTestcaseId());
        vo = executeMgr.startHG(useableList, app, tc.getId(), tc.getCaseName(), taskCode);
        vo.setTaskCode(taskCode);
        vo.addUnableDevice(unableDevices);
        return ok(vo);
    }

    @ApiOperation(value="执行一个回归测试任务")
    @ResponseBody
    @PostMapping(value = "/run/hg/scripts")
    public Result run(@RequestBody TaskNewStartByScriptsForm form) throws ObjectNotExistsException, AuthorizedException, TaskStartException {
        appValidator.validateAppExist(form.getAppId());
        if(form.getScriptIds() == null || form.getScriptIds().size() == 0) {
            throw new ParamsIsNullException("参数错误，脚本为空");
        }
        scriptValidator.validateScriptsExist(form.getScriptIds());

        taskValidatoer.validateAppAndDevicePlatform(form.getAppId(), form.getDeviceIds());

        App app = appService.findOne(form.getAppId());

        scriptValidator.validateScriptBelongApp(form.getScriptIds(), app.getPackageName());

        User user = userService.findByUsername(WebUtil.getCurrentUsername());

        Set<String> onlineDeviceIdList = deviceAuthMgr.allOnlineDevices();
        List<Device> deviceList = deviceService.findAll(form.getDeviceIds());
        List<Device> unableDevices = new ArrayList<>();
        List<String> unableDeviceIds = new ArrayList<>();
        for(Device device : deviceList) {
            if(!onlineDeviceIdList.contains(device.getDeviceId())){
                unableDevices.add(device);
                unableDeviceIds.add(device.getDeviceId());
            } else if (!DB.DeviceWorkStatus.FREE.equals(device.getWorkStatus()) || !DB.DeviceDebugStatus.FREE.equals(device.getDebugStatus())) {
                unableDevices.add(device);
                unableDeviceIds.add(device.getDeviceId());
            }
        }
        List<String> useableList = form.getDeviceIds().stream().filter(item -> !unableDeviceIds.contains(item)).collect(toList());
        TaskStartResultVO vo = new TaskStartResultVO();
        if(useableList.size() == 0) {
            vo.addUnableDevice(unableDevices);
            return ok(vo);
        }
        for(String deviceId : useableList) {
            deviceLockMgr.workLock(deviceId, user.getUserCode(), workExpireTime);
        }
        Long taskCode = taskIdWorker.nextId();
        vo = executeMgr.startHG(useableList, app, form.getScriptIds(), taskCode);
        vo.setTaskCode(taskCode);
        vo.addUnableDevice(unableDevices);
        return ok(vo);
    }

    @ApiOperation(value="执行一个兼容测试任务")
    @ResponseBody
    @PostMapping(value = "/run/jr")
    public Result runJR(@RequestBody TaskNewStartJRForm form) {
        appValidator.validateAppExist(form.getAppId());
        taskValidatoer.validateAppAndDevicePlatform(form.getAppId(), form.getDeviceIds());

        User user = userService.findByUsername(WebUtil.getCurrentUsername());

        Set<String> onlineDeviceIdList = deviceAuthMgr.allOnlineDevices();
        List<Device> deviceList = deviceService.findAll(form.getDeviceIds());
        List<Device> unableDevices = new ArrayList<>();
        List<String> unableDeviceIds = new ArrayList<>();
        for(Device device : deviceList) {
            if(!onlineDeviceIdList.contains(device.getDeviceId())){
                unableDevices.add(device);
                unableDeviceIds.add(device.getDeviceId());
            } else if (!DB.DeviceWorkStatus.FREE.equals(device.getWorkStatus()) || !DB.DeviceDebugStatus.FREE.equals(device.getDebugStatus())) {
                unableDevices.add(device);
                unableDeviceIds.add(device.getDeviceId());
            }
        }
        List<String> useableList = form.getDeviceIds().stream().filter(item -> !unableDeviceIds.contains(item)).collect(toList());
        TaskStartResultVO vo;
        if(useableList.size() == 0) {
            log.error("兼容测试执行失败：没有可用的设备");
            vo = new TaskStartResultVO();

        }else{
            for(String deviceId : useableList) {
                deviceLockMgr.workLock(deviceId, user.getUserCode(), workExpireTime);
            }
            Long taskCode = taskIdWorker.nextId();
            App app = appService.findOne(form.getAppId());
            vo = executeMgr.startJR(useableList, app.getProjectId(), form.getAppId(), taskCode);
            vo.setTaskCode(taskCode);
        }
        vo.addUnableDevice(unableDevices);
        return ok(vo);
    }

    @ApiOperation(value="执行一个遍历测试任务")
    @ResponseBody
    @PostMapping(value = "/run/crawler")
    public Result runCrawler(@RequestBody TaskNewStartCrawlerForm form) {
        appValidator.validateAppExist(form.getAppId());
        taskValidatoer.validateAppAndDevicePlatform(form.getAppId(), form.getDeviceIds());

        User user = userService.findByUsername(WebUtil.getCurrentUsername());

        Set<String> onlineDeviceIdList = deviceAuthMgr.allOnlineDevices();
        List<Device> deviceList = deviceService.findAll(form.getDeviceIds());
        List<Device> unableDevices = new ArrayList<>();
        List<String> unableDeviceIds = new ArrayList<>();
        for(Device device : deviceList) {
            if(!onlineDeviceIdList.contains(device.getDeviceId())){
                unableDevices.add(device);
                unableDeviceIds.add(device.getDeviceId());
            } else if (!DB.DeviceWorkStatus.FREE.equals(device.getWorkStatus()) || !DB.DeviceDebugStatus.FREE.equals(device.getDebugStatus())) {
                unableDevices.add(device);
                unableDeviceIds.add(device.getDeviceId());
            }
        }
        List<String> useableList = form.getDeviceIds().stream().filter(item -> !unableDeviceIds.contains(item)).collect(toList());
        TaskStartResultVO vo;
        if(useableList.size() == 0) {
            log.error("遍历测试执行失败：没有可用的设备");
            vo = new TaskStartResultVO();

        }else{
            for(String deviceId : useableList) {
                deviceLockMgr.workLock(deviceId, user.getUserCode(), workExpireTime);
            }
            Long taskCode = taskIdWorker.nextId();
            App app = appService.findOne(form.getAppId());
            vo = executeMgr.startCrawler(useableList, app.getProjectId(), form.getAppId(), taskCode);
            vo.setTaskCode(taskCode);

            // 保存参数
            Map<String, Object> config = new HashMap<String, Object>(){
                {
                    if(form.getBlackList() != null) put("blackList", form.getBlackList());
                    if(form.getWhiteList() != null) put("whiteList", form.getWhiteList());
                    if(form.getUrlBlackList() != null) put("urlBlackList", form.getUrlBlackList());
                    if(form.getUrlWhiteList() != null) put("urlWhiteList", form.getUrlWhiteList());
                    if(form.getFirstList() != null) put("firstList", form.getFirstList());
                    if(form.getLastList() != null) put("lastList", form.getLastList());
                    if(form.getBackButton() != null) put("backButton", form.getBackButton());
                    if(form.getMaxDepth() != null) put("maxDepth", form.getMaxDepth());
                    if(form.getTrigger() != null) put("trigger", form.getTrigger());
                }
            };

            TaskParams params = new TaskParams();
            params.setTaskCode(taskCode);
            params.setParams(config);
            AppInfo appInfo = appInfoService.getByPackage(app.getProjectId(), app.getPackageName());
            if(appInfo != null) {
                params.setAppInfoId(appInfo.getId() + "");
                params.setAppDisplayName(appInfo.getName());
                params.setAppPackageName(appInfo.getPackageName());
            }
            taskParamsService.save(params);

        }
        vo.addUnableDevice(unableDevices);
        return ok(vo);
    }


    @ApiOperation(value="停止一个设备任务")
    @ResponseBody
    @PostMapping(value = "/stop")
    public Result stop(@RequestBody TaskStopForm form) throws ObjectNotExistsException {
        Task task = taskValidatoer.validateTaskExist(form.getTaskCode());
        if(form.getDeviceIds() != null && form.getDeviceIds().size() > 0 ){
            List<Device> taskDevices = task.getDevices();
            List<Device> notInTaskDevice = taskDevices.stream().filter(device -> !form.getDeviceIds().contains(device.getDeviceId())).collect(toList());
            if (notInTaskDevice != null && notInTaskDevice.size() > 0) {
                throw new ObjectNotExistsException(String.format("设备 %s 不在该任务中", JSON.toJSON(notInTaskDevice)));
            }
            deviceValidatoer.validateOnline(form.getDeviceIds());
        }
        executeMgr.stop(form);
        return ok();
    }

}
