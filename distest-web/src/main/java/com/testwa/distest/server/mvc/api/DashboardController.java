package com.testwa.distest.server.mvc.api;

import com.testwa.distest.server.mvc.beans.Result;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.service.ProjectService;
import com.testwa.distest.server.mvc.service.UserDeviceHisService;
import com.testwa.distest.server.mvc.service.UserService;
import com.testwa.distest.server.mvc.vo.DashboardPlatformVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public Result<Object> platformDashboard() {
        User user = userService.findByUsername(getCurrentUsername());
        Integer projectCounts = projectService.getProjectCountByUser(user);
        Integer deviceCounts = userDeviceHisService.getDeviceCountByUser(user);

        return ok(new DashboardPlatformVO(projectCounts, deviceCounts));
    }


}
