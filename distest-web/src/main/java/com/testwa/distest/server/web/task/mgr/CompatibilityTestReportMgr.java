package com.testwa.distest.server.web.task.mgr;import com.testwa.distest.server.entity.Device;import com.testwa.distest.server.entity.Task;import com.testwa.distest.server.mongo.model.CrashLog;import com.testwa.distest.server.mongo.model.MethodRunningLog;import com.testwa.distest.server.mongo.model.Step;import com.testwa.distest.server.mongo.service.CrashLogService;import com.testwa.distest.server.mongo.service.MethodRunningLogService;import com.testwa.distest.server.mongo.service.StepService;import com.testwa.distest.server.web.task.vo.DeviceProgressVO;import com.testwa.distest.server.web.task.vo.compatibility.CompatibilityDeviceReportInfoVO;import com.testwa.distest.server.web.task.vo.compatibility.CompatibilityTaskBaseInfoVO;import com.testwa.distest.server.web.task.vo.PerformanceDeviceOverviewVO;import lombok.extern.slf4j.Slf4j;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Component;import org.springframework.transaction.annotation.Propagation;import org.springframework.transaction.annotation.Transactional;import java.math.BigDecimal;import java.text.DecimalFormat;import java.util.ArrayList;import java.util.List;/** * @Program: distest * @Description: 兼容测试报告服务 * @Author: wen * @Create: 2018-05-30 11:29 **/@Slf4j@Component@Transactional(propagation = Propagation.REQUIRED, readOnly = true)public class CompatibilityTestReportMgr {    private static final DecimalFormat doubleTwoPointFormat = new DecimalFormat("0.00");    @Autowired    private MethodRunningLogService methodRunningLogService;    @Autowired    private StepService stepService;    @Autowired    private CrashLogService crashLogService;    @Autowired    private ReportMgr reportMgr;    /**     * @Description: 测试结果基本信息，设备数量，测试时长，crash数量，失败步骤数量     * @Param: [task]     * @Return: com.testwa.distest.server.web.task.vo.compatibility.CompatibilityTaskBaseInfoVO     * @Author wen     * @Date 2018/11/16 15:12     */    public CompatibilityTaskBaseInfoVO getBaseInfo(Task task) {        CompatibilityTaskBaseInfoVO vo = new CompatibilityTaskBaseInfoVO();        // 设备数量        vo.setDeviceNum(task.getDevices().size());        // 运行测试时长        List<MethodRunningLog> methodLogs = methodRunningLogService.getRunningTime(task.getTaskCode());        Long startTime = 0L;        Long endTime = 0L;        for (MethodRunningLog l : methodLogs) {            if ("start".equals(l.getFlag())) {                startTime += l.getTimestamp();            }else{                endTime += l.getTimestamp();            }        }        int devSize = task.getDevices().size();        double runTime = (endTime - startTime)*1.0 / devSize / 1000;        BigDecimal bg = BigDecimal.valueOf(runTime);        double runTimeSecond = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();        vo.setTestRuntimeSecond(runTimeSecond);        // crash 数量        List<CrashLog> logs = crashLogService.findBy(task.getTaskCode());        vo.setCrashNum(logs.size());        // debug 数量        List<Step> stepList = stepService.listErrorStatusStep(task.getTaskCode());        vo.setDeviceNum(stepList.size());        return vo;    }    /**     * @Description: 包括性能指标和测试完成指标，测试完成指标包括安装、启动、执行、卸载等。     * @Param: [task]     * @Return: java.util.List<com.testwa.distest.server.web.task.vo.compatibility.CompatibilityDeviceReportInfoVO>     * @Author wen     * @Date 2018/11/16 15:09     */    public List<CompatibilityDeviceReportInfoVO> getDeviceReport(Task task) {        List<CompatibilityDeviceReportInfoVO> result = new ArrayList<>();        List<Device> devices = task.getDevices();        devices.forEach( d-> {            CompatibilityDeviceReportInfoVO drinfo = new CompatibilityDeviceReportInfoVO();            drinfo.setBrand(d.getBrand());            drinfo.setDeviceId(d.getDeviceId());            drinfo.setModel(d.getModel());            drinfo.setOsVersion(d.getOsVersion());            if(d.getWidth() != null && d.getHeight() != null){                drinfo.setResolution(d.getWidth() + "x" + d.getHeight());            }            // 获取测试完成指标            DeviceProgressVO progress = reportMgr.getProgress(task.getTaskCode(), d.getDeviceId());            // 获取性能指标            PerformanceDeviceOverviewVO performance = reportMgr.getPerformanceOverview(task.getTaskCode(), d.getDeviceId());            drinfo.setPoint(progress);            drinfo.setPerformance(performance);            result.add(drinfo);        });        return result;    }}