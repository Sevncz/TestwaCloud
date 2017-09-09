package com.testwa.distest.server.mvc.api;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.exception.NoSuchAppException;
import com.testwa.distest.server.exception.NoSuchTaskException;
import com.testwa.distest.server.exception.NoSuchTestcaseException;
import com.testwa.core.model.RemoteRunCommand;
import com.testwa.core.model.RemoteTestcaseContent;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.mvc.beans.Result;
import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.model.*;
import com.testwa.distest.server.mvc.service.*;
import com.testwa.distest.server.mvc.service.cache.RemoteClientService;
import com.testwa.distest.server.mvc.vo.DeleteVO;
import com.testwa.distest.server.mvc.vo.TaskVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Created by wen on 12/08/2017.
 */
@Api("任务相关api")
@RestController
@RequestMapping(path = "/api/task")
public class TaskController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private ProjectService projectService;
    @Autowired
    private AppService appService;
    @Autowired
    private UserService userService;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private TestcaseService testcaseService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private RemoteClientService remoteClientService;

    private final SocketIOServer server;

    @Autowired
    public TaskController(SocketIOServer server) {
        this.server = server;
    }

    @SuppressWarnings("unused")
    @Data
    public static class TaskInfo {
        public String taskId;
        public String projectId;
        public List<String> caseIds;
        public String appId;
        public String name;
        public List<String> deviceIds;
        public String description;

    }


    @ApiOperation(value="创建和更新任务")
    @ResponseBody
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public Result save(@RequestBody TaskInfo taskInfo){
        String appId = taskInfo.appId;
        String projectId = taskInfo.projectId;
        String name = taskInfo.name;
        List<String> caseIds = taskInfo.caseIds;

        Task task = new Task();
        task.setAppId(appId);
        task.setName(name);
        task.setProjectId(projectId);
        task.setTestcaseIds(caseIds);
        task.setDescription(taskInfo.getDescription());
        task.setCreateDate(TimeUtil.getTimestampLong());
        task.setModifyDate(TimeUtil.getTimestampLong());
        task.setCreator(getCurrentUsername());
        task.setDisable(false);
        taskService.save(task);
        return ok();
    }

    @ResponseBody
    @PostMapping(value = "/modify")
    public Result modify(@Valid @RequestBody TaskInfo modifyTaskVO) throws NoSuchTaskException, NoSuchTestcaseException, NoSuchAppException {
        taskService.modifyTask(modifyTaskVO);
        return ok();
    }

    @ApiOperation(value="任务分页列表")
    @ResponseBody
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Result page(@RequestParam(value = "page") Integer page,
                       @RequestParam(value = "size") Integer size,
                       @RequestParam(value = "sortField") String sortField,
                       @RequestParam(value = "sortOrder") String sortOrder,
                       @RequestParam(required = false) String projectId,
                       @RequestParam(required = false) String appId){

        PageRequest pageRequest = buildPageRequest(page, size, sortField, sortOrder);
        User user = userService.findByUsername(getCurrentUsername());
        List<String> projectIds = new ArrayList<>();
        if(StringUtils.isBlank(projectId)){
            List<Project> projectsOfUser = projectService.findByUser(user);
            projectsOfUser.forEach(item -> projectIds.add(item.getId()));
        }else{
            List<ProjectMember> pms = projectService.getMembersByProjectAndUserId(projectId, user.getId());
            if(pms == null || pms.size() == 0){
                log.error("ProjectMember is null, user {} not in project {}", user.getId(), projectId);
                return fail(ResultCode.INVALID_PARAM, "用户不属于该项目");
            }
            projectIds.add(projectId);
        }
        Page<Task> tasks = taskService.findPage(pageRequest, appId, projectIds);
        Iterator<Task> tasksIter = tasks.iterator();
        List<TaskVO> lists = new ArrayList<>();
        while(tasksIter.hasNext()){
            lists.add(new TaskVO(tasksIter.next()));
        }
        PageResult<TaskVO> pr = new PageResult<>(lists, tasks.getTotalElements());
        return ok(pr);
    }


    @ApiOperation(value="执行并保存一个任务")
    @ResponseBody
    @RequestMapping(value = "/saveAndRun", method = RequestMethod.POST)
    public Result saveAndRun(@RequestBody TaskInfo taskInfo){
        String appId = taskInfo.appId;
        String projectId = taskInfo.projectId;
        List<String> caseIds = taskInfo.caseIds;
        List<String> deviceIds = taskInfo.deviceIds;

        Task task = new Task();
        task.setAppId(appId);
        task.setProjectId(projectId);
        task.setTestcaseIds(caseIds);
        taskService.save(task);

        // 执行...
        // save a report
//        App app = appService.getAppById(appId);
        User user = userService.findByUsername(getCurrentUsername());

        // 执行完成之后再生成报告
//        Report report = new Report(task, app, deviceIds, user);
//        reportService.save(report);

        List<RemoteTestcaseContent> cases = new ArrayList<>();
        for(String caseId : caseIds){

            RemoteTestcaseContent content = new RemoteTestcaseContent();
            content.setTestcaseId(caseId);
            Testcase c = testcaseService.getTestcaseById(caseId);
            content.setScriptIds(c.getScripts());
            cases.add(content);

        }

        for (String key : deviceIds) {

            TDevice d = deviceService.getDeviceById(key);
            if (d == null) {
                log.error(String.format("设备:[%s]找不到", key));
                // TODO 记录该错误
                continue;
            }

            String sessionId = remoteClientService.getClientSessionByDeviceId(key);

            RemoteRunCommand cmd = new RemoteRunCommand();
            cmd.setAppId(appId);
            cmd.setCmd(0);
            cmd.setDeviceId(key);
            cmd.setTestcaseList(cases);

            server.getClient(UUID.fromString(sessionId))
                    .sendEvent(WebsocketEvent.ON_TESTCASE_RUN, JSON.toJSONString(cmd));
        }

        return ok();
    }

    @ApiOperation(value="执行一个任务")
    @ResponseBody
    @RequestMapping(value = "/run", method = RequestMethod.POST)
    public Result run(@RequestBody TaskInfo taskinfo){
        try {
            taskService.run(taskinfo.taskId, taskinfo.deviceIds);
        } catch (Exception e) {
            log.error("session not found ", e);
            return fail(ResultCode.SERVER_ERROR, "设备已断开");
        }
        return ok();
    }

    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = {"application/json"})
    public Result delete(@Valid @RequestBody DeleteVO deleteVO) {
        for (String id : deleteVO.getIds()) {
            taskService.deleteById(id);
        }
        return ok();
    }
    @ApiOperation(value="杀掉一个设备任务")
    @ResponseBody
    @RequestMapping(value = "/kill", method = RequestMethod.POST)
    public Result kill(@RequestBody TaskInfo taskinfo){
        try {
            taskService.run(taskinfo.taskId, taskinfo.deviceIds);
        } catch (Exception e) {
            log.error("session not found ", e);
            return fail(ResultCode.SERVER_ERROR, "设备已断开");
        }
        return ok();
    }


    @ResponseBody
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Result test(@RequestParam(value = "deviceId")String deviceId){
        RemoteRunCommand params = new RemoteRunCommand();
        params.setExeId("");
        params.setAppId("");
        params.setDeviceId("");
        params.setInstall("");
        params.setCmd(1);  // 启动
        String agentSession = remoteClientService.getMainSessionByDeviceId(deviceId);
        server.getClient(UUID.fromString(agentSession))
                    .sendEvent(WebsocketEvent.ON_TESTCASE_RUN, JSON.toJSONString(params));
        return ok();
    }

    @ResponseBody
    @GetMapping(value = "/detail/{taskId}")
    public Result detail(@PathVariable String taskId){
        TaskVO taskVO = taskService.getTaskVO(taskId);
        return ok(taskVO);
    }

}
