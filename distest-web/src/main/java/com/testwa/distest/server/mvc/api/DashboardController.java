package com.testwa.distest.server.mvc.api;

import com.testwa.distest.server.mvc.beans.Result;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.model.UserDeviceHis;
import com.testwa.distest.server.mvc.service.ProjectService;
import com.testwa.distest.server.mvc.service.UserDeviceHisService;
import com.testwa.distest.server.mvc.service.UserService;
import com.testwa.distest.server.mvc.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(path = "dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserDeviceHisService userDeviceHisService;

    @ResponseBody
    @GetMapping(value = "/platform")
    public Result platformDashboard() {
        User user = userService.findByUsername(getCurrentUsername());
        // devices
        List<UserDeviceHis> devices = userDeviceHisService.getUserDevice(user);
        // stats
        Integer projectCounts = projectService.getProjectCountByUser(user);
        Integer deviceCounts = devices.size();
        // project history
        List<Project> projects = projectService.getHistoryUserProjectVO(user);
        List<ProjectVO> projectVOs = projectsTOProjectVOs(projects);

        return ok(new DashboardPlatformVO(projectCounts, deviceCounts, projectVOs, devices));
    }

    @ResponseBody
    @GetMapping(value = "/project")
    public Result projectDashboard(@RequestParam String projectId) {
        User user = userService.findByUsername(getCurrentUsername());
        // stats
        ProjectStats projectStats = projectService.getProjectStats(projectId, user);
        // todo: task info 1. execution task running 2. execution task just finished

        return ok(new DashboardProjectVO(projectStats));
    }

    @ResponseBody
    @PostMapping(value = "/project/quickDeploy")
    public Result projectDashboard(@RequestBody QuickDeployVO quickDeployVO) {
        // todo: 1. create case 2. create task 3. run task

        return ok();
    }
}
