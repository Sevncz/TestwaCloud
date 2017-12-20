package com.testwa.distest.server.web.task.controller;

import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.server.entity.DeviceAndroid;
import com.testwa.distest.server.entity.Device;
import com.testwa.distest.server.entity.Task;
import com.testwa.core.utils.TimeUtil;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.distest.server.mvc.model.ProcedureInfo;
import com.testwa.distest.server.mvc.model.ProcedureStatis;
import com.testwa.distest.server.mvc.service.ProcedureInfoService;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.validator.TaskSceneValidatoer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 24/10/2017.
 */
@Log4j2
@Api("任务报告相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/report")
public class ReportController extends BaseController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskSceneValidatoer taskSceneValidatoer;
    @Autowired
    private TaskValidatoer taskValidatoer;
    @Autowired
    private ProcedureInfoService procedureInfoService;

    @ApiOperation(value="任务统计")
    @ResponseBody
    @GetMapping(value = "/task/{taskId}")
    public Result statis(@PathVariable Long taskId) throws ObjectNotExistsException {

        Task task = taskValidatoer.validateTaskExist(taskId);
        Map<String, Object> result = taskService.statis(task);

        return ok(result);
    }

}
