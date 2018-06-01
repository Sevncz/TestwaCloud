package com.testwa.distest.server.web.task.controller;

import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.exception.ParamsIsNullException;
import com.testwa.core.base.form.DeleteAllForm;
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
import com.testwa.distest.server.mongo.model.Step;
import com.testwa.distest.server.mongo.service.CrashLogService;
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
@Api("通用测试任务报告")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/report")
public class CommonReportController extends BaseController {

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
    private StepService stepService;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private CrashLogService crashLogService;
    @Autowired
    private ReportMgr reportMgr;

    @ApiOperation(value="任务基本信息")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}")
    public Result statis(@PathVariable Long taskCode) throws ObjectNotExistsException {

        Task task = taskValidatoer.validateTaskExist(taskCode);
        Map<String, Object> result = taskService.statis(task);

        return ok(result);
    }

    @ApiOperation(value="登录用户可见的任务分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/page")
    public Result page(@Valid TaskListForm pageForm) {
        User user = userService.findByUsername(WebUtil.getCurrentUsername());
        if(pageForm.getProjectId() != null) {
            projectValidator.validateUserIsProjectMember(pageForm.getProjectId(), user.getId());
        }else{
            projectValidator.validateUserInAnyProject(WebUtil.getCurrentUsername());
        }
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
        taskService.disableAll(form.getEntityIds());
        return ok();
    }

    @ApiOperation(value="任务脚本列表", notes="")
    @ResponseBody
    @GetMapping(value = "/script/list")
    public Result scriptList(@Valid ScriptListForm form) throws ParamsIsNullException {
        if(form.getTaskCode() == null){
            throw new ParamsIsNullException("TaskCode is null");
        }
        List<Script> scriptList = taskService.findScriptListInTask(form);
        return ok(scriptList);
    }

    @ApiOperation(value="步骤信息列表", notes="")
    @ResponseBody
    @GetMapping(value = "/step/list")
    public Result stepList(@Valid StepListForm form) throws ParamsIsNullException {
        Long taskCode = form.getTaskCode();
        Task task = taskValidatoer.validateTaskExist(taskCode);
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
        Long taskCode = pageForm.getTaskCode();
        Task task = taskValidatoer.validateTaskExist(taskCode);
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
    @GetMapping(value = "/appiumpath/{taskCode}/{deviceId}")
    public Result appiumLogPath(@PathVariable Long taskCode, @PathVariable String deviceId) throws ParamsIsNullException, ObjectNotExistsException {
        if(taskCode == null || deviceId == null ){
            throw new ParamsIsNullException("参数不能为空");
        }
        taskValidatoer.validateTaskExist(taskCode);
        AppiumFile appiumFile = appiumFileService.findOne(taskCode, deviceId);
        if(appiumFile == null){
            throw new ObjectNotExistsException("Appium 日志不存在");
        }
        return ok(appiumFile.buildPath());
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
    public Result progress(@PathVariable(value = "taskCode") Long taskCode) throws ObjectNotExistsException {
        if(taskCode == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        taskValidatoer.validateTaskExist(taskCode);
        TaskProgressVO result = reportMgr.getProgress(taskCode);
        return ok(result);
    }


    @ApiOperation(value="设备的进度")
    @ResponseBody
    @GetMapping(value = "/progress/{taskCode}/{deviceId}")
    public Result progressDevice(@PathVariable(value = "taskCode") Long taskCode, @PathVariable(value = "deviceId") String deviceId) throws ObjectNotExistsException {
        if(taskCode == null || StringUtils.isBlank(deviceId)){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskCode);
        DeviceProgressVO vo = reportMgr.getProgress(taskCode, deviceId);
        return ok(vo);
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
    public Result overview(@PathVariable(value = "taskCode") Long taskCode) throws ObjectNotExistsException {
        if(taskCode == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskCode);
        TaskProgressVO progressVO = reportMgr.getProgress(taskCode);
        TaskOverviewVO overviewVO = reportMgr.getTaskOverview(task);
        TaskDeviceFinishStatisVO finishStatisVO = reportMgr.getFinishStatisVO(taskCode);

        TaskOverallProgressVO result = new TaskOverallProgressVO();
        result.setEquipment(finishStatisVO);
        result.setOverview(overviewVO);
        result.setExecution(progressVO);

        return ok(result);
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
    public Result performance(@PathVariable(value = "taskCode") Long taskCode) throws ObjectNotExistsException {
        if(taskCode == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskCode);
        PerformanceOverviewVO vo = reportMgr.getPerformanceOverview(task);
        return ok(vo);
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
    public Result performanceDevice(@PathVariable(value = "taskCode") Long taskCode, @PathVariable(value = "deviceId") String deviceId) throws ObjectNotExistsException {
        if(taskCode == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskCode);
        PerformanceDeviceOverviewVO vo = reportMgr.getPerformanceOverview(task, deviceId);
        return ok(vo);
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
    public Result performanceDetail(@PathVariable(value = "taskCode") Long taskCode) throws ObjectNotExistsException {
        if(taskCode == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskCode);
        ReportPerformanceDetailVO vo = reportMgr.getPerformanceDetail(task);
        return ok(vo);
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
    public Result performanceSummary(@PathVariable(value = "taskCode") Long taskCode) throws ObjectNotExistsException {
        if(taskCode == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskCode);
        ReportPerformanceSummaryVO vo = reportMgr.getPerformanceSummary(task);
        return ok(vo);
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
    public Result performanceFlowSummary(@PathVariable(value = "taskCode") Long taskCode, @PathVariable(value = "deviceId") String deviceId) throws ObjectNotExistsException {
        if(taskCode == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskCode);
        EchartDoubleLine line = reportMgr.getPerformanceFlowSummary(task, deviceId);

        return ok(line);
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
    public Result crashLog(@PathVariable(value = "taskCode") Long taskCode, @PathVariable(value = "deviceId") String deviceId) throws ObjectNotExistsException {
        if(taskCode == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskCode);
        List<CrashLog> crashLogs = crashLogService.findBy(taskCode, deviceId);
        return ok(crashLogs);
    }

}
