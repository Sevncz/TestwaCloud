package com.testwa.distest.server.web.task.controller;

import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.DeviceNotActiveException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.enums.DB;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.entity.TaskScene;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mongo.service.ExecutorLogInfoService;
import com.testwa.distest.server.service.task.form.TaskStartForm;
import com.testwa.distest.server.service.task.form.TaskStopForm;
import com.testwa.distest.server.service.task.form.TaskStartByTestcaseForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.app.validator.AppValidator;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.task.execute.ExecuteMgr;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.validator.TaskSceneValidatoer;
import com.testwa.distest.server.web.task.vo.TaskProgressVO;
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
@Api("任务执行相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/task")
public class TaskController extends BaseController {

    @Autowired
    private ExecuteMgr executeMgr;
    @Autowired
    private UserService userService;
    @Autowired
    private ExecutorLogInfoService executorLogInfoService;
    @Autowired
    private TaskSceneValidatoer taskSceneValidatoer;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private AppValidator appValidator;
    @Autowired
    private DeviceValidatoer deviceValidatoer;
    @Autowired
    private TaskValidatoer taskValidatoer;


    @ApiOperation(value="保存并执行一个任务场景")
    @ResponseBody
    @PostMapping(value = "/scene/saveandrun")
    public Result saveAndRun(@RequestBody TaskStartByTestcaseForm form) throws ObjectNotExistsException, AuthorizedException {
        projectValidator.validateProjectExist(form.getProjectId());
        appValidator.validateAppExist(form.getAppId());
        deviceValidatoer.validateOnline(form.getDeviceIds());
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(form.getProjectId(), user.getId());

        executeMgr.start(form);

        return ok();
    }

    @ApiOperation(value="执行一个任务场景")
    @ResponseBody
    @PostMapping(value = "/scene/run")
    public Result run(@RequestBody TaskStartForm form) throws ObjectNotExistsException, AuthorizedException, DeviceNotActiveException {

        TaskScene scene = taskSceneValidatoer.validateTaskSceneExist(form.getTaskSceneId());
        deviceValidatoer.validateOnline(form.getDeviceIds());
        deviceValidatoer.validateActive(form.getDeviceIds());
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(scene.getProjectId(), user.getId());

        executeMgr.start(form);
        return ok();
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