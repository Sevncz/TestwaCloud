package com.testwa.distest.server.mvc.api;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.mvc.beans.Result;
import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.model.Project;
import com.testwa.distest.server.mvc.model.User;
import com.testwa.distest.server.mvc.service.AppService;
import com.testwa.distest.server.mvc.service.ProjectService;
import com.testwa.distest.server.mvc.service.ScriptService;
import com.testwa.distest.server.mvc.service.TestcaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by wen on 12/08/2017.
 */
@Api("任务相关api")
@RestController
@RequestMapping(path = "/api/task")
public class TaskController extends BaseController{

    @Autowired
    private ProjectService projectService;
    @Autowired
    private AppService appService;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private TestcaseService testcaseService;

    @SuppressWarnings("unused")
    private static class TaskInfo {
        public List<String> caseIds;
        public String appId;
        public List<String> deviceIds;

    }

    @ApiOperation(value="创建和更新任务")
    @ResponseBody
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public Result save(@RequestBody TaskInfo taskInfo){

        return ok();
    }


    @ApiOperation(value="执行任务")
    @ResponseBody
    @RequestMapping(value = "/run", method = RequestMethod.POST)
    public Result run(@RequestBody TaskInfo taskInfo){
        String appId = taskInfo.appId;
        List<String> caseIds = taskInfo.caseIds;
        List<String> deviceIds = taskInfo.deviceIds;



        return ok();
    }

}
