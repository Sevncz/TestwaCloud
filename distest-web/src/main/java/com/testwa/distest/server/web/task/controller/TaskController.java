package com.testwa.distest.server.web.task.controller;

import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.ObjectNotExistsException;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.enums.DB;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.core.base.constant.WebConstants;
import com.testwa.distest.server.service.task.form.TaskStartForm;
import com.testwa.distest.server.service.task.form.TaskStopForm;
import com.testwa.distest.server.service.task.form.TaskStartByTestcaseForm;
import com.testwa.distest.server.web.app.validator.AppValidator;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.task.execute.ExecuteMgr;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.validator.TaskSceneValidatoer;
import com.testwa.distest.server.web.task.vo.TaskProgressVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Log4j2
@Api("任务部署相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/task")
public class TaskController extends BaseController {

    @Autowired
    private ExecuteMgr executeMgr;
    @Autowired
    private TaskSceneValidatoer taskValidatoer;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private AppValidator appValidator;
    @Autowired
    private DeviceValidatoer deviceValidatoer;
    @Autowired
    private TaskValidatoer executionTaskValidatoer;


    @ApiOperation(value="执行并保存一个任务")
    @ResponseBody
    @PostMapping(value = "/save/run")
    public Result saveAndRun(@RequestBody TaskStartByTestcaseForm form) throws ObjectNotExistsException {
        projectValidator.validateProjectExist(form.getProjectId());
        appValidator.validateAppExist(form.getAppId());
        deviceValidatoer.validateOnline(form.getDeviceIds());

        executeMgr.start(form);

        return ok();
    }

    @ApiOperation(value="执行一个任务")
    @ResponseBody
    @PostMapping(value = "/run")
    public Result run(@RequestBody TaskStartForm form) throws ObjectNotExistsException {

        taskValidatoer.validateTaskSceneExist(form.getTaskSceneId());
        deviceValidatoer.validateOnline(form.getDeviceIds());

        executeMgr.start(form);
        return ok();
    }


    @ApiOperation(value="停止一个设备任务")
    @ResponseBody
    @PostMapping(value = "/kill")
    public Result kill(@RequestBody TaskStopForm form) throws ObjectNotExistsException {
        taskValidatoer.validateTaskSceneExist(form.getTaskSceneId());
        deviceValidatoer.validateOnline(form.getDeviceIds());
        executeMgr.stop(form);
        return ok();
    }

    @ApiOperation(value="查看一个任务的进度")
    @ResponseBody
    @GetMapping(value = "/progress/{taskId}")
    public Result progress(@PathVariable(value = "taskId") Long taskId) throws ObjectNotExistsException {
        executionTaskValidatoer.validateTaskExist(taskId);
        List<TaskProgressVO> result = executeMgr.getProgress(taskId);
        return ok(result);
    }


    @ResponseBody
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Result test(@RequestParam(value = "deviceId")String deviceId){
        RemoteRunCommand params = new RemoteRunCommand();
        params.setExeId(1l);
        params.setAppId(1l);
        params.setDeviceId("");
        params.setInstall("");
        params.setCmd(DB.CommandEnum.START.getValue());  // 启动
//        String agentSession = remoteClientService.getMainSessionByDeviceId(deviceId);
//        server.getClient(UUID.fromString(agentSession))
//                .sendEvent(WebsocketEvent.ON_TESTCASE_RUN, JSON.toJSONString(params));
        return ok();
    }


}
