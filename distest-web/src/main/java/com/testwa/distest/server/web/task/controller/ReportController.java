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
import com.testwa.distest.server.mongo.model.ProcedureInfo;
import com.testwa.distest.server.mongo.service.ProcedureInfoService;
import com.testwa.distest.server.service.task.form.ScriptListForm;
import com.testwa.distest.server.service.task.form.StepListForm;
import com.testwa.distest.server.service.task.form.StepPageForm;
import com.testwa.distest.server.service.task.form.TaskListForm;
import com.testwa.distest.server.service.task.service.AppiumFileService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.task.validator.StepValidatoer;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.vo.*;
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
    private ProcedureInfoService procedureInfoService;
    @Autowired
    private ProjectValidator projectValidator;

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
        if(form.getTaskId() == null){
            throw new ParamsIsNullException("TaskId is null");
        }
        if(form.getScriptId() == null){
            throw new ParamsIsNullException("ScriptId is null");
        }
        if(StringUtils.isBlank(form.getDeviceId())){
            throw new ParamsIsNullException("DeviceId is null");
        }
        List<ProcedureInfo> procedureInfoList = procedureInfoService.findList(form);
        return ok(procedureInfoList);
    }

    @ApiOperation(value="步骤信息分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/step/page")
    public Result stepPage(@Valid StepPageForm pageForm) throws ParamsIsNullException {
        if(pageForm.getTaskId() == null){
            throw new ParamsIsNullException("TaskId is null");
        }
        if(pageForm.getScriptId() == null){
            throw new ParamsIsNullException("ScriptId is null");
        }
        if(StringUtils.isBlank(pageForm.getDeviceId())){
            throw new ParamsIsNullException("DeviceId is null");
        }
        PageResult<ProcedureInfo> procedureInfoPage = procedureInfoService.findByPage(pageForm);
        return ok(procedureInfoPage);
    }

    @ApiOperation(value="当前步骤的下一个步骤", notes="")
    @ResponseBody
    @GetMapping(value = "/step/next/{procedureId}")
    public Result stepNext(@PathVariable String procedureId) throws ParamsIsNullException {
        if(StringUtils.isEmpty(procedureId)){
            throw new ParamsIsNullException("procedureId is null");
        }
        stepValidatoer.validateProcedureExist(procedureId);

        ProcedureInfo nextProcedure = procedureInfoService.findNextById(procedureId);
        return ok(nextProcedure);
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
        TaskProgressVO result = taskService.getProgress(taskId);
        return ok(result);
    }

    /**
     *@Description: 当前任务进度详情查看，包括设备完成度统计、每个设备执行任务过程中每开始一个子任务的时间线、任务最终状态
     *@Param: [taskId]
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
        taskValidatoer.validateTaskExist(taskId);
        TaskProgressVO progressVO = taskService.getProgress(taskId);
        TaskOverviewVO overviewVO = taskService.getOverview(taskId);
        TaskDeviceFinishStatisVO finishStatisVO = taskService.getFinishStatisVO(taskId);

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
    @ApiOperation(value="性能概述")
    @ResponseBody
    @GetMapping(value = "/performance/{taskId}")
    public Result performance(@PathVariable(value = "taskId") Long taskId) throws ObjectNotExistsException {
        if(taskId == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskId);
        PerformanceOverviewVO vo = new PerformanceOverviewVO();
        if (DB.TaskType.HG.equals(task.getTaskType())) {
            vo = taskService.getHGPerformanceOverview(task);
        }else if (DB.TaskType.JR.equals(task.getTaskType())) {
            vo = taskService.getJRPerformanceOverview(task);
        }

        return ok(vo);
    }

    /**
     *@Description: 性能详细信息，供echart线性图使用，包括cpu，内存，fps，流量
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
        ReportPerformanceTimePointVO vo = null;
        if (DB.TaskType.HG.equals(task.getTaskType())) {
            vo = taskService.getHGPerformanceDetail(task);
        }else if (DB.TaskType.JR.equals(task.getTaskType())) {
            vo = taskService.getJRPerformanceDetail(task);
        }

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
    @GetMapping(value = "/performance/detail/{taskId}")
    public Result performanceSummary(@PathVariable(value = "taskId") Long taskId) throws ObjectNotExistsException {
        if(taskId == null){
            throw new ParamsIsNullException("参数不能为空");
        }
        Task task = taskValidatoer.validateTaskExist(taskId);
        ReportPerformanceTimePointVO vo = null;
        if (DB.TaskType.HG.equals(task.getTaskType())) {
            vo = taskService.getHGPerformanceDetail(task);
        }else if (DB.TaskType.JR.equals(task.getTaskType())) {
            vo = taskService.getJRPerformanceDetail(task);
        }

        return ok(vo);
    }

}
