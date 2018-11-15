package com.testwa.distest.server.web.task.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.mongo.model.Step;
import com.testwa.distest.server.mongo.service.StepService;
import com.testwa.distest.server.service.task.form.StepListForm;
import com.testwa.distest.server.service.task.form.StepPageForm;
import com.testwa.distest.server.web.task.validator.StepValidatoer;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@Slf4j
@Api("步骤相关接口")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class StepController extends BaseController {

    @Autowired
    private StepValidatoer stepValidatoer;
    @Autowired
    private StepService stepService;
    @Autowired
    private TaskValidatoer taskValidatoer;

    @ApiOperation(value="步骤信息列表", notes="")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/device/{deviceId}/script/{scriptId}/stepList")
    public List stepList(@PathVariable(value = "taskCode") Long taskCode,
                         @PathVariable(value = "deviceId") String deviceId,
                         @PathVariable(value = "scriptId") Long scriptId,
                         @Valid StepListForm form) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        if (DB.TaskType.FUNCTIONAL.equals(task.getTaskType())) {
            return stepService.listScriptAll(taskCode, deviceId, scriptId);
        }else if (DB.TaskType.COMPATIBILITY.equals(task.getTaskType()) || DB.TaskType.CRAWLER.equals(task.getTaskType())) {
            return stepService.listTaskAll(taskCode, deviceId);
        }
        return Collections.emptyList();
    }

    @ApiOperation(value="步骤信息分页列表", notes="")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/device/{deviceId}/script/{scriptId}/stepPage")
    public PageResult stepPage(@PathVariable(value = "taskCode") Long taskCode,
                               @PathVariable(value = "deviceId") String deviceId,
                               @PathVariable(value = "scriptId") Long scriptId,
                               @Valid StepPageForm pageForm) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        if (DB.TaskType.FUNCTIONAL.equals(task.getTaskType())) {
            return stepService.pageFunctionalStep(taskCode, deviceId, scriptId, pageForm);
        }else if (DB.TaskType.COMPATIBILITY.equals(task.getTaskType())) {
            return stepService.pageCompatibilityStep(taskCode, deviceId, pageForm);
        }
        return null;
    }

    @ApiOperation(value="当前步骤的下一个步骤", notes="")
    @ResponseBody
    @GetMapping(value = "/step/{stepId}/next")
    public Step next(@PathVariable String stepId) {
        stepValidatoer.validateStepExist(stepId);
        return stepService.findNextById(stepId);
    }
}
