package com.testwa.distest.server.mvc.api;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.mvc.beans.Result;
import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.ProjectMember;
import com.testwa.distest.server.mvc.model.Task;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.service.*;
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

    private final SocketIOServer server;

    @Autowired
    public TaskController(SocketIOServer server) {
        this.server = server;
    }

    @SuppressWarnings("unused")
    private static class TaskInfo {
        public String taskId;
        public String projectId;
        public List<String> caseIds;
        public String appId;
        public String name;
        public List<String> deviceIds;

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
        task.setCreator(getCurrentUsername());
        task.setDisable(false);
        taskService.save(task);
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
        PageResult<Task> pr = new PageResult<>(tasks.getContent(), tasks.getTotalElements());
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

        return ok();
    }

    @ApiOperation(value="执行一个任务")
    @ResponseBody
    @RequestMapping(value = "/run", method = RequestMethod.POST)
    public Result run(@RequestBody TaskInfo task){
        String taskId = task.taskId;
        //  查询任务...

        // 执行...
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
    @ResponseBody
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Result test(@RequestParam(value = "sessionId")String sessionId){
        RunTestcaseParams params = new RunTestcaseParams();
        params.setAppId("");
        params.setDeviceId("");
        params.setReportDetailId("");
        params.setInstall("");
        server.getClient(UUID.fromString(sessionId))
                    .sendEvent(WebsocketEvent.ON_TESTCASE_RUN, JSON.toJSONString(params));
        return ok();
    }
    @ResponseBody
    @GetMapping(value = "/detail/{taskId}")
    public Result detail(@PathVariable String taskId){
        TaskVO taskVO = taskService.getTaskVO(taskId);
        return ok(taskVO);
    }
    @Data
    private class RunTestcaseParams{

        private String appId;
        private String deviceId;
        private List<String> scriptIds;
        private String reportDetailId;
        private String install;

    }

}
