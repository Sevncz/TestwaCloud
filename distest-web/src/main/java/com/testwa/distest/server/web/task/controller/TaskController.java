package com.testwa.distest.server.web.task.controller;

import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.constant.WebConstants;
import com.testwa.distest.common.controller.BaseController;
import com.testwa.distest.common.exception.ObjectNotExistsException;
import com.testwa.distest.common.form.DeleteAllForm;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.mvc.entity.Task;
import com.testwa.distest.server.service.task.form.TaskListForm;
import com.testwa.distest.server.service.task.form.TaskNewForm;
import com.testwa.distest.server.service.task.form.TaskUpdateForm;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.vo.TaskVO;
import com.testwa.distest.server.web.testcase.validator.TestcaseValidatoer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by wen on 24/10/2017.
 */
@Api("任务管理相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/task")
public class TaskController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskValidatoer taskValidatoer;
    @Autowired
    private TestcaseValidatoer testcaseValidatoer;

    @ApiOperation(value="创建任务")
    @ResponseBody
    @PostMapping(value = "/save")
    public Result save(@Valid TaskNewForm form){
        log.info(form.toString());
        taskService.save(form);
        return ok();
    }

    @ApiOperation(value="修改任务")
    @ResponseBody
    @PostMapping(value = "/modify")
    public Result modify(@Valid TaskUpdateForm form) throws ObjectNotExistsException {
        log.info(form.toString());
        taskValidatoer.validateTaskExist(form.getTaskId());
        testcaseValidatoer.validateTestcasesExist(form.getCaseIds());
        taskService.update(form);
        return ok();
    }

    @ApiOperation(value="删除任务")
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = {"application/json"})
    public Result delete(@Valid @RequestBody DeleteAllForm form) {
        taskService.delete(form.getEntityIds());
        return ok();
    }

    @ApiOperation(value="任务分页列表")
    @ResponseBody
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Result page(TaskListForm pageForm) {
        PageResult<Task> taskPR = taskService.findByPage(pageForm);
        PageResult<TaskVO> pr = buildVOPageResult(taskPR, TaskVO.class);
        return ok(pr);
    }

    @ResponseBody
    @GetMapping(value = "/detail/{taskId}")
    public Result detail(@PathVariable String taskId){
        TaskVO taskVO = taskService.getTaskVO(taskId);
        return ok(taskVO);
    }


}
