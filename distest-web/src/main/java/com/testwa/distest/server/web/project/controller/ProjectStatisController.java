package com.testwa.distest.server.web.project.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.core.base.exception.ParamsFormatException;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.base.vo.Result;
import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.task.form.TaskListForm;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.project.mgr.ProjectStatisMgr;
import com.testwa.distest.server.web.project.validator.ProjectValidator;
import com.testwa.distest.server.web.project.vo.ProjectStatis;
import com.testwa.distest.server.web.project.vo.ProjectStatisMultiBarVO;
import com.testwa.distest.server.web.project.vo.ProjectStatisTestInfoVO;
import com.testwa.distest.server.web.project.vo.ProjectTestDynamicVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectStatisMgr projectStatisMgr;


    @ApiOperation(value="项目的基本统计信息")
    @ResponseBody
    @GetMapping(value = "/baseinfo/{projectId}")
    public Result baseInfo(@PathVariable Long projectId){
        projectValidator.validateProjectExist(projectId);
        String username = WebUtil.getCurrentUsername();
        User member = userService.findByUsername(username);
        projectValidator.validateUserIsProjectMember(projectId, member.getId());
        ProjectStatis ps = projectService.statis(projectId);
        return ok(ps);
    }

    protected void checkProjectTestStatisParams(@PathVariable Long projectId, @RequestParam(value="startTime" ,required=false) Long startTime, @RequestParam(value="endTime" ,required=false) Long endTime) {
        projectValidator.validateProjectExist(projectId);
        String username = WebUtil.getCurrentUsername();
        User member = userService.findByUsername(username);
        projectValidator.validateUserIsProjectMember(projectId, member.getId());

        if(startTime != null && endTime != null) {
            if (endTime < startTime) {
                throw new ParamsFormatException("结束时间不能小于开始时间");
            }
        }

        if(startTime != null && !validTimestamp(startTime*1000)) {
            throw new ParamsFormatException("开始时间格式错误");
        }
        if(endTime != null && !validTimestamp(endTime*1000)) {
            throw new ParamsFormatException("结束时间格式错误");
        }
    }

    @ApiOperation(value="项目测试基本统计信息，包括测试市场，测试次数，调试市场，上传脚本数量")
    @ResponseBody
    @GetMapping(value = "/{projectId}/test/info")
    public Result testInfo(@PathVariable Long projectId, @RequestParam(value="startTime" ,required=false) Long startTime, @RequestParam(value="endTime" ,required=false) Long endTime){
        checkProjectTestStatisParams(projectId, startTime, endTime);

        ProjectStatisTestInfoVO testInfo = projectStatisMgr.statisTestInfo(projectId, startTime, endTime);

        return ok(testInfo);
    }


    @ApiOperation(value="应用测试统计")
    @ResponseBody
    @GetMapping(value = "/{projectId}/app/count")
    public Result appCount(@PathVariable Long projectId, @RequestParam(value="startTime" ,required=false) Long startTime, @RequestParam(value="endTime" ,required=false) Long endTime){
        checkProjectTestStatisParams(projectId, startTime, endTime);

        ProjectStatisMultiBarVO appCountVO = projectStatisMgr.statisAppTestCountForEveryTestType(projectId, startTime, endTime);

        return ok(appCountVO);
    }

    @ApiOperation(value="成员测试统计")
    @ResponseBody
    @GetMapping(value = "/{projectId}/member/count")
    public Result memberTimes(@PathVariable Long projectId, @RequestParam(value="startTime" ,required=false) Long startTime, @RequestParam(value="endTime" ,required=false) Long endTime){
        checkProjectTestStatisParams(projectId, startTime, endTime);

        ProjectStatisMultiBarVO memberCountVO = projectStatisMgr.statisMemberTestCountForEveryTestType(projectId, startTime, endTime);

        return ok(memberCountVO);
    }

    @ApiOperation(value="项目测试动态")
    @ResponseBody
    @GetMapping(value = "/{projectId}/dynamic")
    public Result dynamic(@PathVariable Long projectId, @RequestParam(value="startTime" ,required=false) Long startTime, @RequestParam(value="endTime" ,required=false) Long endTime, TaskListForm taskListForm){
        checkProjectTestStatisParams(projectId, startTime, endTime);

        PageResult<ProjectTestDynamicVO> dynamicVOPageResult = projectStatisMgr.dynamicTestPage(projectId, startTime, endTime, taskListForm);

        List<ProjectTestDynamicVO> projectTestDynamicVOs = dynamicVOPageResult.getPages();
        Map<String, List<ProjectTestDynamicVO>> groupByTime = projectTestDynamicVOs.stream().collect(Collectors.groupingBy( vo -> new DateTime(vo.getTime()).toString("yyyy-MM-dd")));

        List result = new ArrayList();
        Set<String> timeGroupSet = groupByTime.keySet();
        List<String> timeGroupList = new ArrayList<>(timeGroupSet);
        Collections.reverse(timeGroupList);
        timeGroupList.forEach( t -> {
            List<ProjectTestDynamicVO> pvo = groupByTime.get(t);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("date", t);
            resultMap.put("data", pvo);
            result.add(resultMap);
        });

        PageResult<Object> resultPR = new PageResult<Object>(result, dynamicVOPageResult.getTotal());

        return ok(resultPR);
    }



}
