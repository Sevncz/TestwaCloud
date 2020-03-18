package com.testwa.distest.server.web.script.controller;

import com.alibaba.fastjson.JSON;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.util.VoUtil;
import com.testwa.core.script.util.FileUtil;
import com.testwa.core.script.vo.ExcutorVO;
import com.testwa.core.script.vo.ScriptCaseVO;
import com.testwa.core.script.vo.TaskEnvVO;
import com.testwa.core.script.vo.TaskVO;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.fdfs.service.FdfsStorageService;
import com.testwa.distest.server.service.script.service.ScriptCaseService;
import com.testwa.distest.server.service.script.service.ScriptCaseSetService;
import com.testwa.distest.server.service.script.service.ScriptMetadataService;
import com.testwa.distest.server.service.task.form.TaskV2StartByScriptSetForm;
import com.testwa.distest.server.service.task.form.TaskV2StartByScriptsForm;
import com.testwa.distest.server.service.task.service.TaskEnvService;
import com.testwa.distest.server.service.task.service.TaskResultService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.web.app.validator.AppValidator;
import com.testwa.distest.server.web.device.mgr.DeviceLockMgr;
import com.testwa.distest.server.web.device.mgr.DeviceOnlineMgr;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.script.validator.ScriptCaseSetValidator;
import com.testwa.distest.server.web.script.validator.ScriptValidator;
import com.testwa.distest.server.web.task.mgr.ExecuteMgr;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.vo.TaskStartResultVO;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Slf4j
@Api(value = "脚本执行相关api", tags = "V2.0")
@Validated
@RestController
@RequestMapping("/v2")
public class ScriptRunnerController extends BaseController {

    @Autowired
    private ExecuteMgr executeMgr;
    @Autowired
    private ScriptCaseService scriptCaseService;
    @Autowired
    private ScriptCaseSetService scriptCaseSetService;
    @Autowired
    private AppValidator appValidator;
    @Autowired
    private DeviceValidatoer deviceValidatoer;
    @Autowired
    private TaskValidatoer taskValidatoer;
    @Autowired
    private ScriptValidator scriptValidator;
    @Autowired
    private ScriptCaseSetValidator scriptCaseSetValidator;
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
    @Value("${base.report.dir}")
    private String reportDir;
    @Autowired
    private FdfsStorageService fdfsStorageService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private TaskEnvService taskEnvService;

    @ApiOperation(value = "通过脚本运行任务", notes = "")
    @ResponseBody
    @PostMapping(value = "/run/functional/byScripts")
    public TaskStartResultVO run(@RequestBody @Valid TaskV2StartByScriptsForm form) {
        appValidator.validateAppExist(form.getAppId());
        appValidator.validateAppInPorject(form.getAppId(), form.getProjectId());
        scriptValidator.validateScriptCasesExist(form.getScriptCaseIds());

        taskValidatoer.validateAppAndDevicePlatform(form.getAppId(), form.getDeviceIds());

        App app = appService.get(form.getAppId());

        scriptValidator.validateScriptCaseBelongApp(form.getScriptCaseIds(), app.getPackageName());

        Set<String> onlineDeviceIdList = deviceOnlineMgr.allOnlineDevices();
        List<Device> deviceList = deviceService.findAll(form.getDeviceIds());
        List<Device> unableDevices = new ArrayList<>();
        List<String> unableDeviceIds = new ArrayList<>();
        for (Device device : deviceList) {
            if (!DB.PhoneOnlineStatus.ONLINE.equals(device.getOnlineStatus())
                    || !DB.DeviceWorkStatus.FREE.equals(device.getWorkStatus())
                    || !DB.DeviceDebugStatus.FREE.equals(device.getDebugStatus())) {
                unableDevices.add(device);
                unableDeviceIds.add(device.getDeviceId());
            }
        }
        List<String> useableList = form.getDeviceIds().stream().filter(item -> !unableDeviceIds.contains(item)).collect(toList());
        TaskStartResultVO vo = new TaskStartResultVO();
        if (useableList.isEmpty()) {
            vo.addUnableDevice(unableDevices);
            return vo;
        }
        for (String deviceId : useableList) {
            deviceLockMgr.workLock(deviceId, currentUser.getUserCode(), workExpireTime);
        }
        vo = executeMgr.startScriptsOnDevices(useableList, app, form.getScriptCaseIds());
        vo.addUnableDevice(unableDevices);
        return vo;
    }

    @ApiOperation(value = "通过测试集运行任务", notes = "")
    @ResponseBody
    @PostMapping(value = "/run/functional/bySet")
    public TaskStartResultVO runScriptSet(@RequestBody @Valid TaskV2StartByScriptSetForm form) {
        appValidator.validateAppExist(form.getAppId());
        appValidator.validateAppInPorject(form.getAppId(), form.getProjectId());
        scriptCaseSetValidator.validateScriptCaseSetIdExist(form.getScriptCaseSetId());

        taskValidatoer.validateAppAndDevicePlatform(form.getAppId(), form.getDeviceIds());

        App app = appService.get(form.getAppId());

//        Set<String> onlineDeviceIdList = deviceOnlineMgr.allOnlineDevices();
        List<Device> deviceList = deviceService.findAll(form.getDeviceIds());
        List<Device> unableDevices = new ArrayList<>();
        List<String> unableDeviceIds = new ArrayList<>();
        for (Device device : deviceList) {
            if (!DB.PhoneOnlineStatus.ONLINE.equals(device.getOnlineStatus())
                    || !DB.DeviceWorkStatus.FREE.equals(device.getWorkStatus())
                    || !DB.DeviceDebugStatus.FREE.equals(device.getDebugStatus())) {
                unableDevices.add(device);
                unableDeviceIds.add(device.getDeviceId());
            }
        }
        List<String> useableList = form.getDeviceIds().stream().filter(item -> !unableDeviceIds.contains(item)).collect(toList());
        TaskStartResultVO vo = new TaskStartResultVO();
        if (useableList.isEmpty()) {
            vo.addUnableDevice(unableDevices);
            return vo;
        }
        for (String deviceId : useableList) {
            deviceLockMgr.workLock(deviceId, currentUser.getUserCode(), workExpireTime);
        }
        vo = executeMgr.startScriptSetOnDevices(useableList, app, form.getScriptCaseSetId());
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
        taskVO.setScriptCases(Collections.singletonList(scriptCaseDetailVO));
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
        taskVO.setScriptCases(Collections.singletonList(scriptCaseDetailVO));
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
        String deviceId = result.getDeviceId();
        deviceLockMgr.workRelease(deviceId);
        return result;
    }

    @ApiOperation(value = "任务执行完成", notes = "")
    @ResponseBody
    @PostMapping(value = "/task/{taskCode}/finish/{status}")
    public String finish(@PathVariable Long taskCode, @PathVariable Integer status, @RequestBody TaskEnvVO envVO) {
        Task task = taskService.findByCode(taskCode);
        if(status == 0) {
            task.setStatus(DB.TaskStatus.COMPLETE);
        }
        if(status != 0) {
            task.setStatus(DB.TaskStatus.ERROR);
        }
        task.setEndTime(new Date());
        taskService.update(task);
        TaskEnv taskEnv = taskEnvService.getByTaskCodeAndDeviceId(taskCode, envVO.getDeviceId());
        boolean isExist = taskEnv != null;
        if(isExist) {
            taskEnv.setUpdateTime(new Date());
            taskEnv.setUpdateBy(currentUser.getId());
            taskEnv.setAgentVersion(envVO.getAgentVersion());
            taskEnv.setJavaVersion(envVO.getJavaVersion());
            taskEnv.setNodeVersion(envVO.getNodeVersion());
            taskEnv.setOsVersion(envVO.getOsVersion());
            taskEnv.setPythonVersion(envVO.getPythonVersion());
            taskEnvService.update(taskEnv);
        }else{
            taskEnv = VoUtil.buildVO(envVO, TaskEnv.class);
            taskEnv.setProjectId(task.getProjectId());
            taskEnv.setCreateTime(new Date());
            taskEnv.setCreateBy(currentUser.getId());
            taskEnv.setTaskCode(taskCode);
            taskEnvService.insert(taskEnv);
        }
        long number = redissonClient.getAtomicLong("task::number::" + taskCode).addAndGet(-1);
        if(number <= 0) {
            // 触发结束
            try {
                runReport(taskCode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "任务"+taskCode+"结束";
    }

    @ApiOperation(value = "根据Result生成report", notes = "")
    @ResponseBody
    @PostMapping(value = "/task/{taskCode}/report")
    public String report(@PathVariable Long taskCode) throws IOException {
        // 下载到目录
        runReport(taskCode);
        return "成功";
    }

    private void runReport(Long taskCode) throws IOException {
        Path taskPath = Paths.get(reportDir, taskCode.toString());
        if (Files.notExists(taskPath)) {
            Files.createDirectory(taskPath);
        }
        Path resultPath = Paths.get(taskPath.toString(), "result");
        FileUtil.ensureExistEmptyDir(resultPath.toString());
        if(Files.notExists(resultPath)) {
            Files.createDirectory(resultPath);
        }
        Path reportPath = Paths.get(taskPath.toString(), "report");

        List<TaskResult> results = taskResultService.listByCode(taskCode);
        for (TaskResult result : results) {
            fdfsStorageService.downloadResult(result, resultPath);
        }

        Task task = taskService.findByCode(taskCode);
        App app = task.getApp();
        List<Device> devices = task.getDevices();
        List<TaskEnv> taskEnvList = taskEnvService.listByCode(taskCode);
        Map<String, String> taskEnvMap = new HashMap<>();
        for (TaskEnv taskEnv : taskEnvList) {
            TaskEnvVO vo = VoUtil.buildVO(taskEnv, TaskEnvVO.class);
            taskEnvMap.put(taskEnv.getDeviceId(), JSON.toJSONString(vo));
        }


        Map<String, Object> model = new HashMap<>();
        model.put("app", app);
        model.put("devices", devices);
        model.put("taskEnvMap", taskEnvMap);
        Template template = freeMarkerConfigurer.getConfiguration().getTemplate("py_environment.ftl");
        try {
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            Files.write(Paths.get(resultPath.toString(), "environment.xml"), content.getBytes(), StandardOpenOption.CREATE);
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        ExcutorVO excutorVO = new ExcutorVO();
        excutorVO.setReportName(task.getTaskName());
        excutorVO.setBuildName(task.getTaskName() + "#" + task.getId());
        String excutorContent = JSON.toJSONString(excutorVO);
        Files.write(Paths.get(resultPath.toString(), "executor.json"), excutorContent.getBytes(), StandardOpenOption.CREATE);

        // 执行allure generate ./result -o ./report/ --clean
        String[] cmd = {"allure", "generate", resultPath.toString(), "-o", reportPath.toString(), "--clean"};
        Runtime.getRuntime().exec(cmd);
    }
}
