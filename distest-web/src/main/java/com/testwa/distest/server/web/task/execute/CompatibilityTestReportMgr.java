package com.testwa.distest.server.web.task.execute;import com.testwa.distest.server.entity.Device;import com.testwa.distest.server.entity.Task;import com.testwa.distest.server.mongo.model.CrashLog;import com.testwa.distest.server.mongo.model.MethodRunningLog;import com.testwa.distest.server.mongo.model.Step;import com.testwa.distest.server.mongo.service.CrashLogService;import com.testwa.distest.server.mongo.service.MethodRunningLogService;import com.testwa.distest.server.mongo.service.PerformanceService;import com.testwa.distest.server.mongo.service.StepService;import com.testwa.distest.server.service.task.service.SubTaskService;import com.testwa.distest.server.service.task.service.TaskService;import com.testwa.distest.server.web.task.vo.DeviceProgressVO;import com.testwa.distest.server.web.task.vo.JR.JRDeviceReportInfoVO;import com.testwa.distest.server.web.task.vo.JRTaskBaseInfoVO;import com.testwa.distest.server.web.task.vo.PerformanceDeviceOverviewVO;import lombok.extern.slf4j.Slf4j;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Component;import java.math.BigDecimal;import java.text.DecimalFormat;import java.util.ArrayList;import java.util.List;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-30 11:29 **/@Slf4j@Componentpublic class CompatibilityTestReportMgr {    private static final DecimalFormat doubleTwoPointFormat = new DecimalFormat("0.00");    @Autowired    private TaskService taskService;    @Autowired    private SubTaskService subTaskService;    @Autowired    private MethodRunningLogService methodRunningLogService;    @Autowired    private StepService stepService;    @Autowired    private PerformanceService performanceService;    @Autowired    private CrashLogService crashLogService;    @Autowired    private ReportMgr reportMgr;    public JRTaskBaseInfoVO getBaseInfo(Task task) {        JRTaskBaseInfoVO vo = new JRTaskBaseInfoVO();        // 设备数量        vo.setDeviceNum(task.getDevices().size());        // 运行测试时长        List<MethodRunningLog> methodLogs = methodRunningLogService.getRunningTime(task.getTaskCode());        Long startTime = 0L;        Long endTime = 0L;        for (MethodRunningLog l : methodLogs) {            if ("start".equals(l.getFlag())) {                startTime += l.getTimestamp();            }else{                endTime += l.getTimestamp();            }        }        int devSize = task.getDevices().size();        Double runTime = (endTime - startTime)*1.0 / devSize / 1000;        BigDecimal bg = new BigDecimal(runTime);        Double runTimeSecond = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();        vo.setTestRuntimeSecond(runTimeSecond);        // crash 数量        List<CrashLog> logs = crashLogService.findBy(task.getTaskCode());        vo.setCrashNum(logs.size());        // debug 数量        List<Step> stepList = stepService.findErrorStatusList(task.getTaskCode());        vo.setDeviceNum(stepList.size());        return vo;    }    public List<JRDeviceReportInfoVO> getDeviceInfo(Task task) {        List<JRDeviceReportInfoVO> result = new ArrayList<>();        List<Device> devices = task.getDevices();        devices.forEach( d-> {            JRDeviceReportInfoVO drinfo = new JRDeviceReportInfoVO();            drinfo.setBrand(d.getBrand());            drinfo.setDeviceId(d.getDeviceId());            drinfo.setModel(d.getModel());            drinfo.setOsVersion(d.getOsVersion());            if(d.getWidth() != null && d.getHeight() != null){                drinfo.setResolution(d.getWidth() + "x" + d.getHeight());            }            // 获取测试完成指标            DeviceProgressVO progress = reportMgr.getProgress(task.getTaskCode(), d.getDeviceId());            // 获取性能指标            PerformanceDeviceOverviewVO performance = reportMgr.getPerformanceOverview(task.getTaskCode(), d.getDeviceId());            drinfo.setPoint(progress);            drinfo.setPerformance(performance);            result.add(drinfo);        });        return result;    }}