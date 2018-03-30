package com.testwa.distest.server.web.task.controller;

import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.AuthorizedException;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.exception.ParamsIsNullException;
import com.testwa.core.base.form.DeleteAllForm;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.entity.AppiumFile;
import com.testwa.distest.server.entity.Task;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mongo.model.ProcedureInfo;
import com.testwa.distest.server.mongo.service.ProcedureInfoService;
import com.testwa.distest.server.service.task.form.StepListForm;
import com.testwa.distest.server.service.task.form.StepPageForm;
import com.testwa.distest.server.service.task.form.TaskListForm;
import com.testwa.distest.server.service.task.service.AppiumFileService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.task.validator.StepValidatoer;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.validator.TaskSceneValidatoer;
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

    @ApiOperation(value="任务统计")
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

    @ApiOperation(value="步骤信息列表", notes="")
    @ResponseBody
    @GetMapping(value = "/step/list")
    public Result stepList(@Valid StepListForm form) throws ParamsIsNullException {
        if(form.getTaskId() == null){
            throw new ParamsIsNullException("TaskId is null");
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

}
