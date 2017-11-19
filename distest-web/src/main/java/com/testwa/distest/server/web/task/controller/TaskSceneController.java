package com.testwa.distest.server.web.task.controller;

import com.testwa.distest.server.entity.TaskScene;
import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.constant.WebConstants;
import com.testwa.distest.common.controller.BaseController;
import com.testwa.distest.common.exception.ObjectNotExistsException;
import com.testwa.distest.common.form.DeleteAllForm;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.service.task.form.TaskSceneListForm;
import com.testwa.distest.server.service.task.form.TaskSceneNewForm;
import com.testwa.distest.server.service.task.form.TaskSceneUpdateForm;
import com.testwa.distest.server.service.task.service.TaskSceneService;
import com.testwa.distest.server.web.app.validator.AppValidator;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.task.validator.TaskSceneValidatoer;
import com.testwa.distest.server.web.task.vo.TaskSceneVO;
import com.testwa.distest.server.web.testcase.validator.TestcaseValidatoer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by wen on 24/10/2017.
 */
@Log4j2
@Api("任务管理相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/task/scene")
public class TaskSceneController extends BaseController {
    @Autowired
    private TaskSceneService taskSceneService;
    @Autowired
    private TaskSceneValidatoer taskSceneValidatoer;
    @Autowired
    private TestcaseValidatoer testcaseValidatoer;
    @Autowired
    private AppValidator appValidator;
    @Autowired
    private ProjectValidator projectValidator;

    @ApiOperation(value="创建任务场景")
    @ResponseBody
    @PostMapping(value = "/save")
    public Result save(@Valid TaskSceneNewForm form){
        log.info(form.toString());
        taskSceneService.save(form);
        return ok();
    }

    @ApiOperation(value="修改任务场景")
    @ResponseBody
    @PostMapping(value = "/modify")
    public Result modify(@Valid TaskSceneUpdateForm form) throws ObjectNotExistsException {
        log.info(form.toString());
        projectValidator.validateProjectExist(form.getAppId());
        appValidator.validateAppExist(form.getAppId());
        testcaseValidatoer.validateTestcasesExist(form.getCaseIds());
        taskSceneValidatoer.validateTaskSceneExist(form.getTaskSceneId());
        taskSceneService.update(form);
        return ok();
    }

    @ApiOperation(value="删除任务场景")
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = {"application/json"})
    public Result delete(@Valid @RequestBody DeleteAllForm form) {
        taskSceneService.delete(form.getEntityIds());
        return ok();
    }

    @ApiOperation(value="任务场景分页列表")
    @ResponseBody
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Result page(TaskSceneListForm pageForm) {
        PageResult<TaskScene> taskPR = taskSceneService.findPageForCurrentUser(pageForm);
        PageResult<TaskSceneVO> pr = buildVOPageResult(taskPR, TaskSceneVO.class);
        return ok(pr);
    }

    @ResponseBody
    @GetMapping(value = "/detail/{taskSceneId}")
    public Result detail(@PathVariable Long taskSceneId){
        TaskSceneVO taskSceneVO = taskSceneService.getTaskSceneVO(taskSceneId);
        return ok(taskSceneVO);
    }


}
