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
import com.testwa.distest.server.mongo.model.Step;
import com.testwa.distest.server.mongo.service.CrashLogService;
import com.testwa.distest.server.mongo.service.StepService;
import com.testwa.distest.server.service.task.form.ScriptListForm;
import com.testwa.distest.server.service.task.form.StepListForm;
import com.testwa.distest.server.service.task.form.StepPageForm;
import com.testwa.distest.server.service.task.form.TaskListForm;
import com.testwa.distest.server.service.task.service.AppiumFileService;
import com.testwa.distest.server.service.task.service.SubTaskService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.task.execute.ReportMgr;
import com.testwa.distest.server.web.task.validator.StepValidatoer;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.vo.*;
import com.testwa.distest.server.web.task.vo.echart.EchartDoubleLine;
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
@RequestMapping(path = WebConstants.API_PREFIX + "/report")
public class CommonReportController extends BaseController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private SubTaskService subTaskService;
    @Autowired
    private AppiumFileService appiumFileService;
    @Autowired
    private UserService userService;
    @Autowired
    private TaskValidatoer taskValidatoer;
    @Autowired
    private StepValidatoer stepValidatoer;
    @Autowired
    private StepService stepService;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private CrashLogService crashLogService;
    @Autowired
    private ReportMgr reportMgr;

    @ApiOperation(value="任务分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/{projectId}/page")
    public PageResult page(@PathVariable Long projectId, @Valid TaskListForm pageForm) {
        projectValidator.validateProjectExist(projectId);
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(projectId, user.getId());

        return taskService.findPage(projectId, pageForm);
    }

    @ApiOperation(value="已完成任务分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/finish/{projectId}/page")
    public PageResult finishPage(@PathVariable Long projectId, @Valid TaskListForm pageForm) {
        projectValidator.validateProjectExist(projectId);
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        projectValidator.validateUserIsProjectMember(projectId, user.getId());

        return taskService.findFinishPage(projectId, pageForm);
    }


    @ApiOperation(value="任务基本信息")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}")
    public Map statis(@PathVariable Long taskCode) {

        Task task = taskValidatoer.validateTaskExist(taskCode);
        return taskService.statis(task);
    }


    @ApiOperation(value="删除报告", notes="")
    @ResponseBody
    @PostMapping(value = "/delete")
    public void delete(@RequestBody @Valid IDListForm form) {
        taskService.disableAll(form.getEntityIds());
    }

    @ApiOperation(value="任务脚本列表", notes="")
    @ResponseBody
    @GetMapping(value = "/script/list")
    public List scriptList(@Valid ScriptListForm form) {
        return taskService.findScriptListInTask(form);
    }

    @ApiOperation(value="步骤信息列表", notes="")
    @ResponseBody
    @GetMapping(value = "/step/list")
    public List stepList(@Valid StepListForm form) {
        Long taskCode = form.getTaskCode();
        Task task = taskValidatoer.validateTaskExist(taskCode);
        if (DB.TaskType.FUNCTIONAL.equals(task.getTaskType())) {
            return stepService.listScriptAll(form);
        }else if (DB.TaskType.COMPATIBILITY.equals(task.getTaskType()) || DB.TaskType.CRAWLER.equals(task.getTaskType())) {
            return stepService.listTaskAll(form);
        }
        return null;
    }

    @ApiOperation(value="步骤信息分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/step/page")
    public PageResult stepPage(@Valid StepPageForm pageForm) {
        Long taskCode = pageForm.getTaskCode();
        Task task = taskValidatoer.validateTaskExist(taskCode);
        if (DB.TaskType.FUNCTIONAL.equals(task.getTaskType())) {
            return stepService.pageFunctionalStep(pageForm);
        }else if (DB.TaskType.COMPATIBILITY.equals(task.getTaskType())) {
            return stepService.pageCompatibilityStep(pageForm);
        }
        return null;
    }

    @ApiOperation(value="当前步骤的下一个步骤", notes="")
    @ResponseBody
    @GetMapping(value = "/step/next/{stepId}")
    public Step stepNext(@PathVariable String stepId) {
        stepValidatoer.validateStepExist(stepId);
        return stepService.findNextById(stepId);
    }

    @ApiOperation(value="返回一个appium日志相对路径", notes="")
    @ResponseBody
    @GetMapping(value = "/appiumpath/{taskCode}/{deviceId}")
    public Result appiumLogPath(@PathVariable Long taskCode, @PathVariable String deviceId) {
        taskValidatoer.validateTaskExist(taskCode);
        AppiumFile appiumFile = appiumFileService.findOne(taskCode, deviceId);
        if(appiumFile == null){
            Result.error(ResultCode.ILLEGAL_OP, "Appium 日志不存在");
        }
        return Result.success(appiumFile.buildPath());
    }


    /**
     *@Description: 执行进度详情，每个设备执行任务过程中每开始一个子任务的时间线
     *@Param: [taskCode]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="返回任务中每个设备的执行进度")
    @ResponseBody
    @GetMapping(value = "/progress/{taskCode}")
    public TaskProgressVO progress(@PathVariable(value = "taskCode") Long taskCode) {

        Task task = taskValidatoer.validateTaskExist(taskCode);
        TaskProgressVO result = reportMgr.getProgress(taskCode);
        result.setStatus(task.getStatus());
        return result;
    }


    @ApiOperation(value="设备的进度")
    @ResponseBody
    @GetMapping(value = "/progress/{taskCode}/{deviceId}")
    public DeviceProgressVO progressDevice(@PathVariable(value = "taskCode") Long taskCode, @PathVariable(value = "deviceId") String deviceId) {

        Task task = taskValidatoer.validateTaskExist(taskCode);
        return reportMgr.getProgress(taskCode, deviceId);
    }

    /**
     *@Description: 当前任务进度详情查看，包括设备完成度统计、每个设备执行任务过程中每开始一个子任务的时间线、任务最终状态
     *@Param: [taskCode]  `
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="任务进度统计")
    @ResponseBody
    @GetMapping(value = "/overview/{taskCode}")
    public TaskOverallProgressVO overview(@PathVariable(value = "taskCode") Long taskCode) {

        Task task = taskValidatoer.validateTaskExist(taskCode);
        TaskProgressVO progressVO = reportMgr.getProgress(taskCode);
        TaskOverviewVO overviewVO = reportMgr.getTaskOverview(task);
        TaskDeviceFinishStatisVO finishStatisVO = reportMgr.getFinishStatisVO(taskCode);

        TaskOverallProgressVO result = new TaskOverallProgressVO();
        result.setEquipment(finishStatisVO);
        result.setOverview(overviewVO);
        result.setExecution(progressVO);

        return result;
    }

    /**
     *@Description: 平均性能概述，包括安装时长、启动时长、cpu平均使用率、内存平均使用量、平均fps帧率、下行流量、上行流量
     *@Param: [taskCode]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="平均性能概述")
    @ResponseBody
    @GetMapping(value = "/performance/{taskCode}")
    public PerformanceOverviewVO performance(@PathVariable(value = "taskCode") Long taskCode) {

        Task task = taskValidatoer.validateTaskExist(taskCode);
        return reportMgr.getPerformanceOverview(task);
    }

    /**
     *@Description: 设备性能的基本概述， 包括安装时长、启动时长、cpu平均使用率、内存平均使用量、平均fps帧率、下行流量、上行流量
     *@Param: [taskCode, deviceId]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/25
     */
    @ApiOperation(value="设备性能的基本概述")
    @ResponseBody
    @GetMapping(value = "/performance/{taskCode}/{deviceId}")
    public PerformanceDeviceOverviewVO performanceDevice(@PathVariable(value = "taskCode") Long taskCode, @PathVariable(value = "deviceId") String deviceId) {

        Task task = taskValidatoer.validateTaskExist(taskCode);
        return reportMgr.getPerformanceOverview(task.getTaskCode(), deviceId);
    }

    /**
     *@Description: 性能详细信息，供echart线性图使用，包括cpu，内存，fps
     *@Param: [taskCode]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="性能详细信息")
    @ResponseBody
    @GetMapping(value = "/performance/detail/{taskCode}")
    public ReportPerformanceDetailVO performanceDetail(@PathVariable(value = "taskCode") Long taskCode) {

        Task task = taskValidatoer.validateTaskExist(taskCode);
        return reportMgr.getPerformanceDetail(task);
    }

    /**
     *@Description: 性能综合数据，饼图和柱形图数据
     *@Param: [taskCode]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="性能详细信息")
    @ResponseBody
    @GetMapping(value = "/performance/summary/{taskCode}")
    public ReportPerformanceSummaryVO performanceSummary(@PathVariable(value = "taskCode") Long taskCode) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        return reportMgr.getPerformanceSummary(task);
    }

    /**
     *@Description: 流量数据，上下行
     *@Param: [taskCode]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="流量详细信息")
    @ResponseBody
    @GetMapping(value = "/performance/detail/flow/{taskCode}/{deviceId}")
    public EchartDoubleLine performanceFlowSummary(@PathVariable(value = "taskCode") Long taskCode, @PathVariable(value = "deviceId") String deviceId) {

        Task task = taskValidatoer.validateTaskExist(taskCode);

        return reportMgr.getPerformanceFlowSummary(task, deviceId);
    }

    /**
     *@Description: crash日志列表获取
     *@Param: [taskCode, deviceId]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/25
     */
    @ApiOperation(value="crash日志")
    @ResponseBody
    @GetMapping(value = "/crash/log/{taskCode}/{deviceId}")
    public List crashLog(@PathVariable(value = "taskCode") Long taskCode, @PathVariable(value = "deviceId") String deviceId) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        return crashLogService.findBy(taskCode, deviceId);
    }


    @ApiOperation(value="测试通过情况")
    @ResponseBody
    @GetMapping(value = "/passinfo/{taskCode}")
    public Result passInfo(@PathVariable Long taskCode) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        if(DB.TaskStatus.RUNNING.equals(task.getStatus())) {
            Result.error(ResultCode.CONFLICT, "任务还未完成");
        }
        return Result.success(reportMgr.getPassInfo(task));
    }


    @ApiOperation(value="启动的详细信息")
    @ResponseBody
    @GetMapping(value = "/{taskCode}/launch/detail/{deviceId}")
    public Result launch(@PathVariable Long taskCode, @PathVariable String deviceId) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        if(DB.TaskStatus.RUNNING.equals(task.getStatus())) {
            return Result.error(ResultCode.CONFLICT, "任务还未完成");
        }
        Step step = stepService.getLaunchStep(taskCode, deviceId);
        return Result.success(step);
    }


    @ApiOperation(value="设备执行任务的信息")
    @ResponseBody
    @GetMapping(value = "/{taskCode}/subTask/info/{deviceId}")
    public Result subTask(@PathVariable Long taskCode, @PathVariable String deviceId) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        if(DB.TaskStatus.RUNNING.equals(task.getStatus())) {
            return Result.error(ResultCode.CONFLICT, "任务还未完成");
        }
        SubTask subTask = subTaskService.findOne(taskCode, deviceId);
        return Result.success(subTask);
    }

}
