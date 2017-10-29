package com.testwa.distest.server.web.dashboard.controller;

import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.constant.WebConstants;
import com.testwa.distest.common.controller.BaseController;
import com.testwa.distest.common.exception.AccountException;
import com.testwa.core.entity.User;
import com.testwa.distest.server.mvc.service.DashboardService;
import com.testwa.distest.server.mvc.vo.QuickDeployVO;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.task.service.TaskSceneService;
import com.testwa.distest.server.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

@Controller
@RequestMapping(path = WebConstants.API_PREFIX + "/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;
//    @Autowired
//    private UserDeviceHisService userDeviceHisService;
    @Autowired
    private TaskSceneService taskService;
    @Autowired
    private DashboardService dashboardService;

    @ResponseBody
    @GetMapping(value = "/platform")
    public Result platformDashboard() throws AccountException {
        User user = userService.findByUsername(getCurrentUsername());
        // devices
//        List<UserDeviceHis> devices = userDeviceHisService.getUserDevice(auth);
        // stats
//        Integer projectCounts = projectService.getProjectCountByUser(auth);
//        Integer deviceCounts = devices.size();
        // project history
//        List<Project> projects = projectService.getHistoryUserProjectVO(auth);
//        List<ProjectVO> projectVOs = projectsTOProjectVOs(projects);

//        return ok(new DashboardPlatformVO(projectCounts, deviceCounts, projectVOs, devices));
        return ok();
    }

    @ResponseBody
    @GetMapping(value = "/project")
    public Result projectDashboard(@RequestParam String projectId) throws AccountException {
        User user = userService.findByUsername(getCurrentUsername());
        // stats
//        ProjectStats projectStats = projectService.getProjectStats(projectId, auth);
        // task info 1. execution task running 2. execution task just finished
//        List<Task> runningTask = taskService.getRunningTask(projectId, auth);
//        List<Task> recentFinishedTask = taskService.getRecentFinishedRunningTask(projectId, auth);
//        return ok(new DashboardProjectVO(projectStats, runningTask, recentFinishedTask));
        return ok();
    }

    @ResponseBody
    @PostMapping(value = "/project/quickDeploy")
    public Result projectDashboard(@RequestBody QuickDeployVO quickDeployVO) throws Exception{
//        User auth = userService.findByUsername(getCurrentUsername());
//        // 1. create case 2. create task 3. run task
//        String executionTaskId = dashboardService.quickDeploy(auth, quickDeployVO);
//        return ok(executionTaskId);
        return ok();
    }
}
