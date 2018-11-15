package com.testwa.distest.server.web.task.controller;

import com.testwa.core.base.constant.WebConstants;
import com.testwa.core.base.controller.BaseController;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.web.task.execute.ReportMgr;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import com.testwa.distest.server.web.task.vo.PerformanceDeviceOverviewVO;
import com.testwa.distest.server.web.task.vo.PerformanceOverviewVO;
import com.testwa.distest.server.web.task.vo.ReportPerformanceDetailVO;
import com.testwa.distest.server.web.task.vo.ReportPerformanceSummaryVO;
import com.testwa.distest.server.web.task.vo.echart.EchartDoubleLine;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api("性能报告")
@Validated
@RestController
@RequestMapping(path = WebConstants.API_PREFIX)
public class PerformanceReportController extends BaseController {
    @Autowired
    private TaskValidatoer taskValidatoer;
    @Autowired
    private ReportMgr reportMgr;

    /**
     *@Description: 平均性能概述，包括安装时长、启动时长、cpu平均使用率、内存平均使用量、平均fps帧率、下行流量、上行流量
     *@Param: [taskCode]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="平均性能概述")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/performanceOverview")
    public PerformanceOverviewVO performanceOverview(@PathVariable(value = "taskCode") Long taskCode) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        return reportMgr.getPerformanceOverview(task);
    }

    /**
     *@Description: 设备性能的基本概述， 包括安装时长、启动时长、cpu平均使用率、内存平均使用量、平均fps帧率、下行流量、上行流量
     *@Param: [taskCode, deviceId]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/25
     */
    @ApiOperation(value="设备性能的基本概述")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/device/{deviceId}/performanceOverview")
    public PerformanceDeviceOverviewVO performanceDevice(@PathVariable(value = "taskCode") Long taskCode, @PathVariable(value = "deviceId") String deviceId) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        return reportMgr.getPerformanceOverview(task.getTaskCode(), deviceId);
    }

    /**
     *@Description: 性能详细信息，供echart线性图使用，包括cpu，内存，fps
     *@Param: [taskCode]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="性能详细信息")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/performanceDetail")
    public ReportPerformanceDetailVO performanceDetail(@PathVariable(value = "taskCode") Long taskCode) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        return reportMgr.getPerformanceDetail(task);
    }

    /**
     *@Description: 性能综合数据，饼图和柱形图数据
     *@Param: [taskCode]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="性能详细信息")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/performanceSummary")
    public ReportPerformanceSummaryVO performanceSummary(@PathVariable(value = "taskCode") Long taskCode) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        return reportMgr.getPerformanceSummary(task);
    }

    /**
     *@Description: 流量数据，上下行
     *@Param: [taskCode]
     *@Return: com.testwa.core.base.vo.Result
     *@Author: wen
     *@Date: 2018/5/24
     */
    @ApiOperation(value="流量详细信息")
    @ResponseBody
    @GetMapping(value = "/task/{taskCode}/device/{deviceId}/flowSummary")
    public EchartDoubleLine flowSummary(@PathVariable(value = "taskCode") Long taskCode, @PathVariable(value = "deviceId") String deviceId) {
        Task task = taskValidatoer.validateTaskExist(taskCode);
        return reportMgr.getFlowSummary(task, deviceId);
    }
}
