package com.testwa.distest.server.web.task.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.entity.Testcase;
import com.testwa.distest.server.mongo.service.ExecutorLogInfoService;
import com.testwa.distest.server.service.script.form.ScriptNewForm;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.service.task.form.*;
import com.testwa.distest.server.service.testcase.service.TestcaseService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.app.validator.AppValidator;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.task.execute.ExecuteMgrV2;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.vo.TaskProgressVO;
import com.testwa.distest.server.web.testcase.validator.TestcaseValidatoer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Slf4j
@Api("任务执行相关api，第二版")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/task")
public class TaskControllerV2 extends BaseController {
    private static final String JR_MD5 = "jrcs_md5";

    @Autowired
    private ExecuteMgrV2 executeMgr;
    @Autowired
    private UserService userService;
    @Autowired
    private ExecutorLogInfoService executorLogInfoService;
    @Autowired
    private TestcaseValidatoer testcaseValidatoer;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private AppValidator appValidator;
    @Autowired
    private DeviceValidatoer deviceValidatoer;
    @Autowired
    private TaskValidatoer taskValidatoer;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private TestcaseService testcaseService;


    @ApiOperation(value="执行一个回归测试任务")
    @ResponseBody
    @PostMapping(value = "/run")
    public Result run(@RequestBody TaskNewByCaseAndStartForm form) throws ObjectNotExistsException, AuthorizedException {
        appValidator.validateAppExist(form.getAppId());
        deviceValidatoer.validateOnline(form.getDeviceIds());
        testcaseValidatoer.validateTestcaseExist(form.getTestcaseId());
        Long taskId = executeMgr.startHG(form);
        return ok(taskId);
    }

    @ApiOperation(value="执行一个兼容测试任务")
    @ResponseBody
    @PostMapping(value = "/run/jr")
    public Result runJR(@RequestBody TaskNewStartJRForm form) throws ObjectNotExistsException, AuthorizedException {
        appValidator.validateAppExist(form.getAppId());
        deviceValidatoer.validateOnline(form.getDeviceIds());
        ScriptNewForm scriptNewForm = new ScriptNewForm();
        scriptNewForm.setProjectId(form.getProjectId());
        List<Script> scriptList = scriptService.findByMD5InProject(JR_MD5, form.getProjectId());
        Script script = null;
        if(scriptList == null || scriptList.size() == 0){
            // 生成一个脚本
            script = scriptService.saveScript("jr.py", "jr.py", JR_MD5, "jr/jr.py", "0", "py", scriptNewForm);
        }else{
            script = scriptList.get(0);
        }

        List<Testcase> testcaseList = testcaseService.findSysJR(form.getProjectId());
        Long testcaseId = null;
        if(testcaseList == null || testcaseList.size() == 0){
            // 生成一个案例
            testcaseId = testcaseService.saveJRTestcase(form.getProjectId(), script.getId());
        }else{
            Testcase testcase = testcaseList.get(0);
            testcaseId = testcase.getId();
        }
        Long taskId = executeMgr.startJR(form, testcaseId);
        return ok(taskId);
    }


    @ApiOperation(value="停止一个设备任务")
    @ResponseBody
    @PostMapping(value = "/stop")
    public Result stop(@RequestBody TaskStopForm form) throws ObjectNotExistsException {
        taskValidatoer.validateTaskExist(form.getTaskId());
        if(form.getDeviceIds() != null && form.getDeviceIds().size() > 0 ){
            deviceValidatoer.validateOnline(form.getDeviceIds());
        }
        executeMgr.stop(form);
        return ok();
    }


    @ApiOperation(value="查看一个任务的进度")
    @ResponseBody
    @GetMapping(value = "/progress/{taskId}")
    public Result progress(@PathVariable(value = "taskId") Long taskId) throws ObjectNotExistsException {
        taskValidatoer.validateTaskExist(taskId);
        List<TaskProgressVO> result = executeMgr.getProgress(taskId);
        return ok(result);
    }

}
