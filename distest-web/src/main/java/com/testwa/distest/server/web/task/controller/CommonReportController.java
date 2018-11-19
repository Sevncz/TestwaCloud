package com.testwa.distest.server.web.task.controller;

import com.testwa.core.base.constant.ResultCode;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.form.IDListForm;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.entity.*;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.distest.server.service.task.form.TaskListForm;
import com.testwa.distest.server.service.task.service.SubTaskService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.task.mgr.ReportMgr;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 24/10/2017.
 */
@Slf4j
@Api("通用测试任务报告")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class CommonReportController extends BaseController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private SubTaskService subTaskService;
    @Autowired
    private UserService userService;
    @Autowired
    private TaskValidatoer taskValidatoer;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private ReportMgr reportMgr;

    @ApiOperation(value="任务分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/reportPage")
    public PageResult page(@PathVariable Long projectId, @Valid TaskListForm pageForm) {
        projectValidator.validateProjectExist(projectId);
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(projectId, user.getId());

        return taskService.findPage(projectId, pageForm);
    }

    @ApiOperation(value="已完成任务分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/project/{projectId}/finishedPage")
    public PageResult finishedPage(@PathVariable Long projectId, @Valid TaskListForm pageForm) {
        projectValidator.validateProjectExist(projectId);
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(projectId, user.getId());

        return taskService.findFinishPage(projectId, pageForm);
    }

    @ApiOperation(value="任务基本信息")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/taskStatis")
    public Map taskStatis(@PathVariable Long taskCode) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        return taskService.statis(task);
    }

    @ApiOperation(value="删除报告", notes="")
    @ResponseBody
    @PostMapping(value = "/reportDelete")
    public void delete(@RequestBody @Valid IDListForm form) {
        taskService.disableAll(form.getEntityIds());
    }

    @ApiOperation(value="任务脚本列表", notes="")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/scripts")
    public List scriptList(@PathVariable Long taskCode) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        return task.getScriptList();
    }


    @ApiOperation(value="测试通过情况")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/passInfo")
    public Result passInfo(@PathVariable Long taskCode) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        if(DB.TaskStatus.RUNNING.equals(task.getStatus())) {
            Result.error(ResultCode.CONFLICT, "任务还未完成");
        }
        return Result.success(reportMgr.getPassInfo(task));
    }


    @ApiOperation(value="设备执行任务的信息")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/device/{deviceId}/subTask")
    public Result subTask(@PathVariable Long taskCode, @PathVariable String deviceId) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        if(DB.TaskStatus.RUNNING.equals(task.getStatus())) {
            return Result.error(ResultCode.CONFLICT, "任务还未完成");
        }
        SubTask subTask = subTaskService.findOne(taskCode, deviceId);
        return Result.success(subTask);
    }

}
