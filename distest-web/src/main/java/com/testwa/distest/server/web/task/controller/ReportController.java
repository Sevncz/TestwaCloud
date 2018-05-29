package com.testwa.distest.server.web.task.controller;

import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.exception.ParamsIsNullException;
import com.testwa.core.base.form.DeleteAllForm;
import com.testwa.core.base.util.StringUtil;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.entity.AppiumFile;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.entity.Task;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mongo.model.CrashLog;
import com.testwa.distest.server.mongo.model.AppiumRunningLog;
import com.testwa.distest.server.mongo.model.Step;
import com.testwa.distest.server.mongo.service.CrashLogService;
import com.testwa.distest.server.mongo.service.AppiumRunningLogService;
import com.testwa.distest.server.mongo.service.StepService;
import com.testwa.distest.server.service.task.form.ScriptListForm;
import com.testwa.distest.server.service.task.form.StepListForm;
import com.testwa.distest.server.service.task.form.StepPageForm;
import com.testwa.distest.server.service.task.form.TaskListForm;
import com.testwa.distest.server.service.task.service.AppiumFileService;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 24/10/2017.
 */
@Slf4j
@Api("任务报告相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/report")
public class ReportController extends BaseController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private AppiumFileService appiumFileService;
    @Autowired
    private UserService userService;
    @Autowired
    private TaskValidatoer taskValidatoer;
    @Autowired
    private StepValidatoer stepValidatoer;
    @Autowired
    private AppiumRunningLogService procedureInfoService;
    @Autowired
    private StepService stepService;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private CrashLogService crashLogService;
    @Autowired
    private ReportMgr reportMgr;

    @ApiOperation(value="任务基本信息")
    @ResponseBody
    @GetMapping(value = "/task/{taskId}")
    public Result statis(@PathVariable Long taskId) throws ObjectNotExistsException {

        Task task = taskValidatoer.validateTaskExist(taskId);
        Map<String, Object> result = taskService.statis(task);

        return ok(result);
    }

    @ApiOperation(value="登录用户可见的任务分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/page")
    public Result page(@Valid TaskListForm pageForm) {

        PageResult<Task> taskPR = taskService.findPageForCurrentUser(pageForm);

        return ok(taskPR);
    }

    @ApiOperation(value="登录用户执行的任务列表", notes="")
    @ResponseBody
    @GetMapping(value = "/my/page")
    public Result myTaskPage(@Valid TaskListForm pageForm) throws AuthorizedException {

        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        if(pageForm.getProjectId() != null){
            projectValidator.validateUserIsProjectMember(pageForm.getProjectId(), user.getId());
        }

        PageResult<Task> taskPR = taskService.findPageForCreateUser(pageForm, user.getId());
        return ok(taskPR);
    }

    @ApiOperation(value="删除报告", notes="")
    @ResponseBody
    @PostMapping(value = "/delete")
    public Result delete(@RequestBody @Valid DeleteAllForm form) {
        taskService.deleteTask(form.getEntityIds());
        return ok();
    }

    @ApiOperation(value="任务脚本列表", notes="")
    @ResponseBody
    @GetMapping(value = "/script/list")
    public Result scriptList(@Valid ScriptListForm form) throws ParamsIsNullException {
        if(form.getTaskId() == null){
            throw new ParamsIsNullException("TaskId is null");
        }
        List<Script> scriptList = taskService.findScriptListInTask(form);
        return ok(scriptList);
    }

    @ApiOperation(value="步骤信息列表", notes="")
    @ResponseBody
    @GetMapping(value = "/step/list")
    public Result stepList(@Valid StepListForm form) throws ParamsIsNullException {
        Long taskId = form.getTaskId();
        Task task = taskValidatoer.validateTaskExist(taskId);
        if (DB.TaskType.HG.equals(task.getTaskType())) {
            List<Step> HGList = stepService.findHGList(form);
            return ok(HGList);
        }else if (DB.TaskType.JR.equals(task.getTaskType())) {
            List<Step> JRList = stepService.findJRList(form);
            return ok(JRList);
        }
        return ok();
    }

    @ApiOperation(value="步骤信息分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/step/page")
    public Result stepPage(@Valid StepPageForm pageForm) throws ParamsIsNullException {
        Long taskId = pageForm.getTaskId();
        Task task = taskValidatoer.validateTaskExist(taskId);
        if (DB.TaskType.HG.equals(task.getTaskType())) {
            PageResult<Step> hgPage = stepService.findHGByPage(pageForm);
            return ok(hgPage);
        }else if (DB.TaskType.JR.equals(task.getTaskType())) {
            PageResult<Step> jrPage = stepService.findJRByPage(pageForm);
            return ok(jrPage);
        }
        return ok();
    }

    @ApiOperation(value="当前步骤的下一个步骤", notes="")
    @ResponseBody
    @GetMapping(value = "/step/next/{stepId}")
    public Result stepNext(@PathVariable String stepId) throws ParamsIsNullException {
        if(StringUtils.isEmpty(stepId)){
            throw new ParamsIsNullException("stepId is null");
        }
        stepValidatoer.validateStepExist(stepId);

        Step nextStep = stepService.findNextById(stepId);
        return ok(nextStep);
    }

    @ApiOperation(value="返回一个appium日志相对路径", notes="")
    @ResponseBody
    @GetMapping(value = "/appiumpath/{taskId}/{deviceId}")
    public Result appiumLogPath(@PathVariable Long taskId, @PathVariable String deviceId) throws ParamsIsNullException, ObjectNotExistsException {
        if(taskId == null || deviceId == null ){
            throw new ParamsIsNullException("参数不能为空");
        }
        taskValidatoer.validateTaskExist(taskId);
        AppiumFile appiumFile = appiumFileService.findOne(taskId, deviceId);
        if(appiumFile == null){
            throw new ObjectNotExistsException("Appium 日志不存在");
        }
        return ok(appiumFile.buildPath());
    }


    /**
     *@Description: 执行进度详情，每个设备执行任务过程中每开始一个子任务的时间线
     *@Param: [taskId]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="返回任务中每个设备的执行进度")
    @ResponseBody
    @GetMapping(value = "/progress/{taskId}")
    public Result progress(@PathVariable(value = "taskId") Long taskId) throws ObjectNotExistsException {
        if(taskId == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        taskValidatoer.validateTaskExist(taskId);
        TaskProgressVO result = reportMgr.getProgress(taskId);
        return ok(result);
    }


    @ApiOperation(value="设备的进度")
    @ResponseBody
    @GetMapping(value = "/progress/{taskId}/{deviceId}")
    public Result progressDevice(@PathVariable(value = "taskId") Long taskId, @PathVariable(value = "deviceId") String deviceId) throws ObjectNotExistsException {
        if(taskId == null || StringUtils.isBlank(deviceId)){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskId);
        DeviceProgressVO vo = reportMgr.getProgress(taskId, deviceId);
        return ok(vo);
    }

    /**
     *@Description: 当前任务进度详情查看，包括设备完成度统计、每个设备执行任务过程中每开始一个子任务的时间线、任务最终状态
     *@Param: [taskId]  `
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="任务进度统计")
    @ResponseBody
    @GetMapping(value = "/overview/{taskId}")
    public Result overview(@PathVariable(value = "taskId") Long taskId) throws ObjectNotExistsException {
        if(taskId == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskId);
        TaskProgressVO progressVO = reportMgr.getProgress(taskId);
        TaskOverviewVO overviewVO = reportMgr.getTaskOverview(task);
        TaskDeviceFinishStatisVO finishStatisVO = reportMgr.getFinishStatisVO(taskId);

        TaskOverallProgressVO result = new TaskOverallProgressVO();
        result.setEquipment(finishStatisVO);
        result.setOverview(overviewVO);
        result.setExecution(progressVO);

        return ok(result);
    }

    /**
     *@Description: 平均性能概述，包括安装时长、启动时长、cpu平均使用率、内存平均使用量、平均fps帧率、下行流量、上行流量
     *@Param: [taskId]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="平均性能概述")
    @ResponseBody
    @GetMapping(value = "/performance/{taskId}")
    public Result performance(@PathVariable(value = "taskId") Long taskId) throws ObjectNotExistsException {
        if(taskId == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskId);
        PerformanceOverviewVO vo = reportMgr.getPerformanceOverview(task);
        return ok(vo);
    }

    /**
     *@Description: 设备性能的基本概述， 包括安装时长、启动时长、cpu平均使用率、内存平均使用量、平均fps帧率、下行流量、上行流量
     *@Param: [taskId, deviceId]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/25
     */
    @ApiOperation(value="设备性能的基本概述")
    @ResponseBody
    @GetMapping(value = "/performance/{taskId}/{deviceId}")
    public Result performanceDevice(@PathVariable(value = "taskId") Long taskId, @PathVariable(value = "deviceId") String deviceId) throws ObjectNotExistsException {
        if(taskId == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskId);
        PerformanceDeviceOverviewVO vo = reportMgr.getPerformanceOverview(task, deviceId);
        return ok(vo);
    }

    /**
     *@Description: 性能详细信息，供echart线性图使用，包括cpu，内存，fps
     *@Param: [taskId]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="性能详细信息")
    @ResponseBody
    @GetMapping(value = "/performance/detail/{taskId}")
    public Result performanceDetail(@PathVariable(value = "taskId") Long taskId) throws ObjectNotExistsException {
        if(taskId == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskId);
        ReportPerformanceDetailVO vo = reportMgr.getPerformanceDetail(task);
        return ok(vo);
    }

    /**
     *@Description: 性能综合数据，饼图和柱形图数据
     *@Param: [taskId]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="性能详细信息")
    @ResponseBody
    @GetMapping(value = "/performance/summary/{taskId}")
    public Result performanceSummary(@PathVariable(value = "taskId") Long taskId) throws ObjectNotExistsException {
        if(taskId == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskId);
        ReportPerformanceSummaryVO vo = reportMgr.getPerformanceSummary(task);
        return ok(vo);
    }

    /**
     *@Description: 流量数据，上下行
     *@Param: [taskId]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="流量详细信息")
    @ResponseBody
    @GetMapping(value = "/performance/detail/flow/{taskId}/{deviceId}")
    public Result performanceFlowSummary(@PathVariable(value = "taskId") Long taskId, @PathVariable(value = "deviceId") String deviceId) throws ObjectNotExistsException {
        if(taskId == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskId);
        EchartDoubleLine line = null;
        if (DB.TaskType.HG.equals(task.getTaskType())) {
            line = reportMgr.getHGPerformanceFlowSummary(task, deviceId);
        }else if (DB.TaskType.JR.equals(task.getTaskType())) {
            line = reportMgr.getJRPerformanceFlowSummary(task, deviceId);
        }

        return ok(line);
    }

    /**
     *@Description: crash日志列表获取
     *@Param: [taskId, deviceId]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/25
     */
    @ApiOperation(value="crash日志")
    @ResponseBody
    @GetMapping(value = "/crash/log/{taskId}/{deviceId}")
    public Result crashLog(@PathVariable(value = "taskId") Long taskId, @PathVariable(value = "deviceId") String deviceId) throws ObjectNotExistsException {
        if(taskId == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskId);
        List<CrashLog> crashLogs = crashLogService.findByTaskIdAndDeviceId(taskId, deviceId);
        return ok(crashLogs);
    }

}
