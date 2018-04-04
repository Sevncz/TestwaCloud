package com.testwa.distest.server.web.project.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.project.vo.ProjectStatis;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by wen on 20/10/2017.
 */
@Slf4j
@Api("项目统计相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/project/statis")
public class ProjectStatisController extends BaseController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectValidator projectValidator;


    @ApiOperation(value="项目的基本统计信息")
    @ResponseBody
    @GetMapping(value = "/baseinfo/{projectId}")
    public Result baseInfo(@PathVariable Long projectId){
        projectValidator.validateProjectExist(projectId);
        ProjectStatis ps = projectService.statis(projectId);
        return ok(ps);
    }

}
