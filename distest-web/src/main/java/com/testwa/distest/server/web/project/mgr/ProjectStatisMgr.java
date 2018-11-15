package com.testwa.distest.server.web.project.mgr;


import com.testwa.core.base.constraint.validation.PageOrderValidator;
import com.testwa.core.base.form.RequestListBase;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.App;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.app.service.AppService;
import com.testwa.distest.server.service.device.service.DeviceLogService;
import com.testwa.distest.server.service.project.service.ProjectMemberService;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.script.form.ScriptListForm;
import com.testwa.distest.server.service.script.service.ScriptService;
import com.testwa.distest.server.service.task.dto.CountAppTestStatisDTO;
import com.testwa.distest.server.service.task.dto.CountElapsedTimeStatisDTO;
import com.testwa.distest.server.service.task.dto.CountMemberTestStatisDTO;
import com.testwa.distest.server.service.task.form.TaskListForm;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.project.vo.ProjectStatisElapsedTimeLineVO;
import com.testwa.distest.server.web.project.vo.ProjectStatisMultiBarVO;
import com.testwa.distest.server.web.project.vo.ProjectStatisTestInfoVO;
import com.testwa.distest.server.web.project.vo.ProjectTestDynamicVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectStatisMgr {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private DeviceLogService deviceLogService;
    @Autowired
    private ProjectMemberService projectMemberService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private AppService appService;
    @Autowired
    private UserService userService;


    /**
     * @Description: 统计项目内的测试使用基本情况，包括测试时长，测试次数，调试市场，上传脚本
     * @Param: [projectId, startTime, endTime]
     * @Return: com.testwa.distest.server.web.project.vo.ProjectStatisTestInfoVO
     * @Author wen
     * @Date 2018/9/18 16:06
     */
    public ProjectStatisTestInfoVO statisTestInfo(Long projectId, Long startTime, Long endTime) {

        ProjectStatisTestInfoVO testInfo = new ProjectStatisTestInfoVO();

        List<User> members = projectMemberService.findAllMembers(projectId);
        // 这里只统计每个成员调试设备花费的时间
        Long debugTime = deviceLogService.sumDebugTime(members, startTime*1000, endTime*1000);
        // 统计测试时长和测试次数
        List<Task> taskList =  taskService.findFinishList(projectId, startTime, endTime, new TaskListForm());
        long testCount = 0L;
        long testTime = 0L;

        for (Task t : taskList) {
            testCount++;
            testTime += t.getEndTime().getTime() - t.getCreateTime().getTime();
        }

        List<Script> scripts = scriptService.findList(projectId, startTime, endTime, new ScriptListForm());

        DecimalFormat df = new DecimalFormat("0.0");

        if(debugTime != null) {
            testInfo.setDebugTime(df.format(debugTime/(1000.0*60*60)));
        }else{
            testInfo.setDebugTime("0");
        }
        testInfo.setTestCount(testCount + "");
        testInfo.setTestTime(df.format(testTime/(1000.0*60*60)));
        testInfo.setScriptNum(scripts.size() + "");
        return testInfo;
    }

    /**
     * @Description: 项目内每个应用的测试次数统计
     * @Param: [projectId, startTime, endTime]
     * @Return: com.testwa.distest.server.web.project.vo.ProjectStatisMultiBarVO
     * @Author wen
     * @Date 2018/9/18 16:07
     */
    public ProjectStatisMultiBarVO statisAppTestCountForEveryTestType(Long projectId, Long startTime, Long endTime) {
        ProjectStatisMultiBarVO appCountVO = new ProjectStatisMultiBarVO();
        appCountVO.init();

        List<CountAppTestStatisDTO> countAppTestStatisList = taskService.countAppTest(projectId, startTime, endTime);

        if(countAppTestStatisList.isEmpty()) {
            return appCountVO;
        }

        Set<Long> appIds = new HashSet<>();
        Set<Integer> testTypes = new HashSet<>();

        for(CountAppTestStatisDTO appTestStatisDTO : countAppTestStatisList) {
            appIds.add(appTestStatisDTO.getAppId());
            testTypes.add(appTestStatisDTO.getTaskType());
        }

        List<Long> appIdList = new ArrayList<>(appIds);
        List<App> apps = appService.findAll(appIdList);
        if(!apps.isEmpty()) {
            List<String> appNames = apps.stream().map(App::getDisplayName).collect(Collectors.toList());

            testTypes.forEach(testType -> {
                ProjectStatisMultiBarVO.TestCountSeries series = new ProjectStatisMultiBarVO.TestCountSeries(apps.size());
                series.setName(DB.TaskType.valueOf(testType).getDesc());

                for(int i=0; i<apps.size(); i++) {
                    App app = apps.get(i);
                    final int index = i;
                    for(CountAppTestStatisDTO appTestStatisDTO : countAppTestStatisList) {
                        if(appTestStatisDTO.getTaskType().equals(testType) && appTestStatisDTO.getAppId().equals(app.getId())) {
                            series.add(appTestStatisDTO.getCount(), index);
                        }
                    }

                }
                appCountVO.add(series);

            });

            appCountVO.setLegend(appNames);
        }
        return appCountVO;
    }

    /**
     * @Description: 项目内每个成员的测试次数统计
     * @Param: [projectId, startTime, endTime]
     * @Return: com.testwa.distest.server.web.project.vo.ProjectStatisMultiBarVO
     * @Author wen
     * @Date 2018/9/19 16:41
     */
    public ProjectStatisMultiBarVO statisMemberTestCountForEveryTestType(Long projectId, Long startTime, Long endTime) {

        ProjectStatisMultiBarVO memberCountVO = new ProjectStatisMultiBarVO();
        memberCountVO.init();

        List<CountMemberTestStatisDTO> countMemberTestStatisList = taskService.countMemberTest(projectId, startTime, endTime);
        if(countMemberTestStatisList.isEmpty()) {
            return memberCountVO;
        }


        Set<Long> memberIds = new HashSet<>();
        Set<Integer> testTypes = new HashSet<>();

        for(CountMemberTestStatisDTO appTestStatisDTO : countMemberTestStatisList) {
            memberIds.add(appTestStatisDTO.getMemberId());
            testTypes.add(appTestStatisDTO.getTaskType());
        }

        List<Long> memberIdList = new ArrayList<>(memberIds);
        List<User> users = userService.findAll(memberIdList);
        if(!users.isEmpty()) {
            List<String> userNames = users.stream().map(User::getUsername).collect(Collectors.toList());

            testTypes.forEach(testType -> {
                ProjectStatisMultiBarVO.TestCountSeries series = new ProjectStatisMultiBarVO.TestCountSeries(users.size());
                series.setName(DB.TaskType.valueOf(testType).getDesc());

                for(int i=0; i<users.size(); i++) {
                    User member = users.get(i);
                    final int index = i;
                    for(CountMemberTestStatisDTO memberTestStatisDTO : countMemberTestStatisList) {
                        if(memberTestStatisDTO.getTaskType().equals(testType) && memberTestStatisDTO.getMemberId().equals(member.getId())) {
                            series.add(memberTestStatisDTO.getCount(), index);
                        }
                    }

                }
                memberCountVO.add(series);

            });

            memberCountVO.setLegend(userNames);
        }
        return memberCountVO;
    }

    public PageResult<ProjectTestDynamicVO> dynamicTestPage(Long projectId, Long startTime, Long endTime, TaskListForm taskListForm) {
        taskListForm.setOrder(PageOrderValidator.DESC);
        taskListForm.setOrderBy("createTime");

        PageResult<Task> taskPageResult = taskService.findPage(projectId, taskListForm);

        List<Task> taskList = taskPageResult.getPages();

        List<ProjectTestDynamicVO> dynamicVOs = taskList.stream().map( task -> {
            User user = userService.findOne(task.getCreateBy());
            ProjectTestDynamicVO vo = new ProjectTestDynamicVO();
            vo.setAppName(task.getApp().getDisplayName());
            vo.setEquipments(task.getDevices().size());
            vo.setStatus(task.getStatus().getValue());
            vo.setTestType(task.getTaskType().getDesc());
            vo.setTime(task.getCreateTime());
            vo.setUser(user.getUsername());
            return vo;
        }).collect(Collectors.toList());

        PageResult<ProjectTestDynamicVO> dynamicVOPageResult = new PageResult(dynamicVOs, taskPageResult.getTotal());
        return dynamicVOPageResult;
    }

    public ProjectStatisElapsedTimeLineVO countElapsedTimeByDay(Long projectId, Long startTime, Long endTime) {
        ProjectStatisElapsedTimeLineVO elapsedTimeLineVO = new ProjectStatisElapsedTimeLineVO();
        List<CountElapsedTimeStatisDTO> countElapsedTimeDaysList = taskService.countElapsedTimeByDay(projectId, startTime, endTime);
        if(countElapsedTimeDaysList != null) {
            countElapsedTimeDaysList.forEach( dto -> {
                if(dto != null) {
                    elapsedTimeLineVO.add(dto.getSecond(), dto.getDay());
                }
            });
        }
        return elapsedTimeLineVO;
    }

    public Map<String, ProjectStatisElapsedTimeLineVO> countElapsedTimeForMember(Long projectId, Long startTime, Long endTime) {
        Map<String, ProjectStatisElapsedTimeLineVO> lines = new HashMap<>();
        List<User> members = projectMemberService.findAllMembers(projectId);
        if(!members.isEmpty()) {
            for(User member : members) {
                List<CountElapsedTimeStatisDTO> countElapsedTimeDaysList = taskService.countElapsedTimeByDay(projectId, member.getId(), startTime, endTime);
                if(countElapsedTimeDaysList != null) {
                    ProjectStatisElapsedTimeLineVO vo = new ProjectStatisElapsedTimeLineVO();
                    countElapsedTimeDaysList.forEach( dto -> {
                        if(dto != null) {
                            vo.add(dto.getSecond(), dto.getDay());
                        }

                    });
                    lines.put(member.getUsername(), vo);
                }
            }
        }
        return lines;
    }
}
