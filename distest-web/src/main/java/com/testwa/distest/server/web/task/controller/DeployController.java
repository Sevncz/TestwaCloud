package com.testwa.distest.server.web.task.controller;

import com.testwa.core.common.enums.DB;
import com.testwa.core.entity.transfer.RemoteRunCommand;
import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.constant.WebConstants;
import com.testwa.distest.common.controller.BaseController;
import com.testwa.distest.common.exception.NoSuchProjectException;
import com.testwa.distest.common.exception.ObjectNotExistsException;
import com.testwa.distest.server.mvc.vo.ExeTaskProgressVO;
import com.testwa.distest.server.service.task.form.TaskStartForm;
import com.testwa.distest.server.service.task.form.TaskStopForm;
import com.testwa.distest.server.service.task.form.TaskNewDeployForm;
import com.testwa.distest.server.web.app.validator.AppValidator;
import com.testwa.distest.server.web.device.validator.DeviceValidatoer;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.task.deploy.DeployMgr;
import com.testwa.distest.server.web.task.validator.ExecutionTaskValidatoer;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by wen on 24/10/2017.
 */
@Api("任务部署相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/deploy")
public class DeployController extends BaseController{

    @Autowired
    private DeployMgr deployMgr;
    @Autowired
    private TaskValidatoer taskValidatoer;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private AppValidator appValidator;
    @Autowired
    private DeviceValidatoer deviceValidatoer;
    @Autowired
    private ExecutionTaskValidatoer executionTaskValidatoer;


    @ApiOperation(value="执行并保存一个任务")
    @ResponseBody
    @RequestMapping(value = "/save/run", method = RequestMethod.POST)
    public Result saveAndRun(@RequestBody TaskNewDeployForm form) throws NoSuchProjectException {
        projectValidator.validateProjectExist(form.getProjectId());
        appValidator.validateProject(form.getAppId());

        deployMgr.deploy(form);

        return ok();
    }

    @ApiOperation(value="执行一个任务")
    @ResponseBody
    @RequestMapping(value = "/run", method = RequestMethod.POST)
    public Result run(@RequestBody TaskStartForm form) throws ObjectNotExistsException {

        taskValidatoer.validateTaskExist(form.getTaskId());
        deviceValidatoer.validateOnline(form.getDeviceIds());

        deployMgr.start(form);
        return ok();
    }


    @ApiOperation(value="停止一个设备任务")
    @ResponseBody
    @RequestMapping(value = "/kill", method = RequestMethod.POST)
    public Result kill(@RequestBody TaskStopForm form){
        deployMgr.stop(form);
        return ok();
    }

    @ApiOperation(value="查看一个任务的进度")
    @ResponseBody
    @RequestMapping(value = "/progress/${exeId}", method = RequestMethod.GET)
    public Result progress(@PathVariable(value = "exeId") Long exeId) throws ObjectNotExistsException {
        executionTaskValidatoer.validateExecutionTaskExist(exeId);
        List<ExeTaskProgressVO> result = deployMgr.getProgress(exeId);
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
        params.setCmd(DB.CommandEnum.START);  // 启动
//        String agentSession = remoteClientService.getMainSessionByDeviceId(deviceId);
//        server.getClient(UUID.fromString(agentSession))
//                .sendEvent(WebsocketEvent.ON_TESTCASE_RUN, JSON.toJSONString(params));
        return ok();
    }


}
