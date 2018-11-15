package com.testwa.distest.server.web.task.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.web.task.execute.ReportMgr;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api("测试任务进度")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class ProgressReportController extends BaseController {
    @Autowired
    private TaskValidatoer taskValidatoer;
    @Autowired
    private ReportMgr reportMgr;

    /**
     *@Description: 执行进度详情，每个设备执行任务过程中每开始一个子任务的时间线
     *@Param: [taskCode]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="返回任务中每个设备的执行进度")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/progress")
    public TaskProgressVO progress(@PathVariable(value = "taskCode") Long taskCode) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        TaskProgressVO result = reportMgr.getProgress(taskCode);
        result.setStatus(task.getStatus());
        return result;
    }


    @ApiOperation(value="设备的进度")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/device/{deviceId}/progress")
    public DeviceProgressVO progressDevice(@PathVariable(value = "taskCode") Long taskCode,
                                           @PathVariable(value = "deviceId") String deviceId) {
        taskValidatoer.validateTaskExist(taskCode);
        return reportMgr.getProgress(taskCode, deviceId);
    }

    /**
     *@Description: 当前任务进度详情查看，包括设备完成度统计、每个设备执行任务过程中每开始一个子任务的时间线、任务最终状态
     *@Param: [taskCode]  `
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="任务进度统计")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/overview")
    public TaskOverallProgressVO overview(@PathVariable(value = "taskCode") Long taskCode) {

        Task task = taskValidatoer.validateTaskExist(taskCode);
        TaskProgressVO progressVO = reportMgr.getProgress(taskCode);
        TaskOverviewVO overviewVO = reportMgr.getTaskOverview(task);
        TaskDeviceFinishStatisVO finishStatisVO = reportMgr.getFinishStatisVO(taskCode);

        TaskOverallProgressVO result = new TaskOverallProgressVO();
        result.setEquipment(finishStatisVO);
        result.setOverview(overviewVO);
        result.setExecution(progressVO);

        return result;
    }

}
