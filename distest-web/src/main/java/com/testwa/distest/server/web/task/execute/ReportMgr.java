package com.testwa.distest.server.web.task.execute;import com.testwa.core.utils.TimeUtil;import com.testwa.distest.server.entity.Device;import com.testwa.distest.server.entity.Task;import com.testwa.distest.server.entity.TaskDevice;import com.testwa.distest.server.mongo.model.MethodRunningLog;import com.testwa.distest.server.mongo.model.Performance;import com.testwa.distest.server.mongo.model.Step;import com.testwa.distest.server.mongo.service.MethodRunningLogService;import com.testwa.distest.server.mongo.service.PerformanceService;import com.testwa.distest.server.mongo.service.StepService;import com.testwa.distest.server.service.task.service.TaskDeviceService;import com.testwa.distest.server.service.task.service.TaskService;import com.testwa.distest.server.web.task.vo.*;import com.testwa.distest.server.web.task.vo.echart.*;import io.rpc.testwa.task.StepRequest;import lombok.extern.slf4j.Slf4j;import org.joda.time.DateTime;import org.joda.time.format.DateTimeFormat;import org.joda.time.format.DateTimeFormatter;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Component;import java.math.BigDecimal;import java.text.DecimalFormat;import java.util.*;import java.util.stream.Collectors;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-28 11:57 **/@Slf4j@Componentpublic class ReportMgr {    private static final DecimalFormat doubleTwoPointFormat = new DecimalFormat("0.00");    @Autowired    private TaskService taskService;    @Autowired    private TaskDeviceService taskDeviceService;    @Autowired    private MethodRunningLogService executorLogInfoService;    @Autowired    private StepService stepService;    @Autowired    private PerformanceService performanceService;    public TaskProgressVO getProgress(Long taskCode) {        Task task = taskService.findByCode(taskCode);        List<Device> deviceList = task.getDevices();        Map<String, List<String>> dateMap = new HashMap<>();        Map<String, String> deviceMap = taskService.getDeviceNameMap(deviceList);        List<String> pointList = new ArrayList<String>(){{            add("测试开始");        }};        deviceList.forEach( d -> {            List<String> dataList = new ArrayList<>();            DateTime dt = new DateTime(task.getCreateTime());            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");            dataList.add(fmt.print(dt));            dateMap.put(d.getDeviceId(), dataList);        });        List<MethodRunningLog> loginfos = executorLogInfoService.getLogCheckPointList(taskCode);        for(MethodRunningLog l : loginfos) {            List<String> datas = dateMap.get(l.getDeviceId());            datas.add(TimeUtil.formatTimeStamp(l.getTimestamp()));            String pointName = executorLogInfoService.getCheckPointName(l.getMethodOrder());            if(!pointList.contains(pointName)){                pointList.add(pointName);            }        }        List<TaskProgressLine> lines = new ArrayList<>();        dateMap.forEach( (k, v) -> {            TaskProgressLine line = new TaskProgressLine();            line.setName(deviceMap.get(k));            line.setData(v);            lines.add(line);        });        TaskProgressVO vo = new TaskProgressVO();        vo.setLineList(lines);        vo.setDeviceNameList(new ArrayList<>(deviceMap.values()));        vo.setPointList(pointList);        return vo;    }    public DeviceProgressVO getProgress(Long taskCode, String deviceId) {        List<Step> stepList = stepService.findBy(taskCode, deviceId);        DeviceProgressVO vo = new DeviceProgressVO();        stepList.forEach( l -> {            if(StepRequest.StepAction.downloadApp.name().equals(l.getAction())) {                if(StepRequest.StepStatus.SUCCESS.getNumber() == l.getStatus()){                    vo.setDownloadApp(true);                }            }            if(StepRequest.StepAction.installApp.name().equals(l.getAction())) {                if(StepRequest.StepStatus.SUCCESS.getNumber() == l.getStatus()){                    vo.setInstall(true);                }            }            if(StepRequest.StepAction.launch.name().equals(l.getAction())) {                if(StepRequest.StepStatus.SUCCESS.getNumber() == l.getStatus()){                    vo.setLaunch(true);                }            }            if(StepRequest.StepAction.uninstallApp.name().equals(l.getAction())) {                if(StepRequest.StepStatus.SUCCESS.getNumber() == l.getStatus()){                    vo.setUninstall(true);                }            }            if(StepRequest.StepAction.operation.name().equals(l.getAction())) {                if(StepRequest.StepStatus.FAIL.getNumber() == l.getStatus()){                    vo.setUninstall(false);                }            }        });        return vo;    }    public TaskOverviewVO getTaskOverview(Task task) {        TaskOverviewVO vo = new TaskOverviewVO();        if(task.getEndTime() == null) {            vo.setStatus(TaskOverviewVO.TaskStatus.RUNNING);            return vo;        }        vo.setStatus(TaskOverviewVO.TaskStatus.COMPLETE);        int deviceSize = task.getDevices().size();        List<Step> installList = taskService.getSuccessStep(task.getTaskCode(), StepRequest.StepAction.installApp);        double install = (installList.size() * 1.0 / deviceSize) * 100;        BigDecimal bg = new BigDecimal(install);        install = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();        List<Step> lanucherList = taskService.getSuccessStep(task.getTaskCode(), StepRequest.StepAction.launch);        double lanucher = (lanucherList.size() * 1.0 / deviceSize) * 100;        bg = new BigDecimal(lanucher);        lanucher = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();        List<Step> uninstallList = taskService.getSuccessStep(task.getTaskCode(), StepRequest.StepAction.uninstallApp);        double uninstall = (uninstallList.size() * 1.0 / deviceSize) * 100;        bg = new BigDecimal(uninstall);        uninstall = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();        List<Map> statusStatis = taskService.getStepStatusStatis(task.getTaskCode());        int success = 0;        int fail = 0;        for(Map m : statusStatis){            int status = (int) m.getOrDefault("_id", 0);            if(status == 0){                success = success + (int) m.getOrDefault("count", 0);            }else{                fail = fail + (int) m.getOrDefault("count", 0);            }        }        double conclusion = ( success + fail ) == 0 ? 0 : (success * 1.0 / (success + fail)) * 100;        vo.setConclusion(doubleTwoPointFormat.format(conclusion) + "%");        vo.setValue(Arrays.asList(install, lanucher, uninstall));        return vo;    }    public TaskDeviceFinishStatisVO getFinishStatisVO(Long taskCode) {        TaskDeviceFinishStatisVO vo = new TaskDeviceFinishStatisVO();        List<TaskDevice> taskDeviceList = taskDeviceService.findByTaskCode(taskCode);        if(taskDeviceList.size() == 0){            return vo;        }        List<TaskDeviceFinishGraph> graph = new ArrayList<>();        int running = 0;        int completed = 0;        int extremely = 0;        int cancel = 0;        for(TaskDevice taskDevice : taskDeviceList) {            switch (taskDevice.getStatus()){                case CANCEL:                    cancel++;                    break;                case RUNNING:                    running++;                    break;                case COMPLETE:                    completed++;                    break;                case ERROR:                    extremely++;                    break;            }        }        int progress = (1 - (running/taskDeviceList.size())) * 100;        TaskDeviceFinishStatistics statistics = new TaskDeviceFinishStatistics();        statistics.setCancel(cancel);        statistics.setCompleted(completed);        statistics.setExtremely(extremely);        statistics.setRunning(running);        statistics.setProgress(progress + "%");        if(running > 0) {            TaskDeviceFinishGraph subGraph = new TaskDeviceFinishGraph();            subGraph.setName("运行中");            subGraph.setValue(running);            graph.add(subGraph);        }        if(completed > 0) {            TaskDeviceFinishGraph subGraph = new TaskDeviceFinishGraph();            subGraph.setName("完成");            subGraph.setValue(completed);            graph.add(subGraph);        }        if(extremely > 0) {            TaskDeviceFinishGraph subGraph = new TaskDeviceFinishGraph();            subGraph.setName("异常");            subGraph.setValue(extremely);            graph.add(subGraph);        }        if(cancel > 0) {            TaskDeviceFinishGraph subGraph = new TaskDeviceFinishGraph();            subGraph.setName("取消");            subGraph.setValue(cancel);            graph.add(subGraph);        }        vo.setGraph(graph);        vo.setStatistics(statistics);        return vo;    }    /**     *@Description: 回归测试性能统计     *@Param: [task]     *@Return: com.testwa.distest.server.web.task.vo.PerformanceOverviewVO     *@Author: wen     *@Date: 2018/5/23     */    public PerformanceOverviewVO getHGPerformanceOverview(Task task) {        PerformanceOverviewVO vo = new PerformanceOverviewVO();        Long taskCode = task.getTaskCode();        List<Device> deviceList = task.getDevices();        Map<String, String> deviceMap = taskService.getDeviceNameMap(deviceList);        List<Map> memoryavg = taskService.getMemoryAvg(taskCode);        PerformanceOverview memoryD = getPerformanceDetail(deviceMap, memoryavg);        vo.setRam(memoryD);        List<Map> cpuavg = taskService.getCpuAvg(taskCode);        PerformanceOverview cpuD = getPerformanceDetail(deviceMap, cpuavg);        vo.setCpu(cpuD);        List<Map> install = taskService.getInstallTime(taskCode);        PerformanceOverview installD = getPerformanceDetail(deviceMap, install);        vo.setInstall(installD);        List<Map> startup = taskService.getStartUpTime(taskCode);        PerformanceOverview startupD = getPerformanceDetail(deviceMap, startup);        vo.setStartUp(startupD);        return vo;    }    private PerformanceOverview getPerformanceDetail(Map<String, String> deviceMap, List<Map> avgList) {        PerformanceOverview detail = new PerformanceOverview();        List<String> names = new ArrayList<>();        List<Double> values = new ArrayList<>();        for(Map m : avgList){            String deviceId = (String) m.getOrDefault("_id", "");            String name = deviceMap.get(deviceId);            names.add(name);            Double d = (Double) m.getOrDefault("value", 0);            BigDecimal bg = new BigDecimal(d);            double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();            values.add(f1);        }        detail.setName(names);        detail.setValue(values);        OptionalDouble avg = values.stream().mapToDouble(v -> v).average();        if (avg.isPresent()) {            detail.setOverview(doubleTwoPointFormat.format(avg.getAsDouble()));        }else{            log.error("average wasn't calculated");            detail.setOverview("0");        }        return detail;    }    /**     *@Description: 兼容测试性能统计     *@Param: [task]     *@Return: com.testwa.distest.server.web.task.vo.PerformanceOverviewVO     *@Author: wen     *@Date: 2018/5/23     */    public PerformanceOverviewVO getPerformanceOverview(Task task) {        PerformanceOverviewVO vo = new PerformanceOverviewVO();        Long taskCode = task.getTaskCode();        List<Device> deviceList = task.getDevices();        Map<String, String> deviceMap = taskService.getDeviceNameMap(deviceList);        List<Map> performanceStatisList = taskService.getPerformanceStatis(taskCode);        PerformanceOverview memoryD = getPerformanceDetail(deviceMap, performanceStatisList, "avg_mem");        vo.setRam(memoryD);        PerformanceOverview cpuD = getPerformanceDetail(deviceMap, performanceStatisList, "avg_cpu");        vo.setCpu(cpuD);        PerformanceOverview fpsD = getPerformanceDetail(deviceMap, performanceStatisList, "avg_fps");        vo.setFps(fpsD);        PerformanceOverview wifiDownD = getPerformanceDetail(deviceMap, performanceStatisList, "sum_wifiDown");        vo.setFlowDown(wifiDownD);        PerformanceOverview wifiUpD = getPerformanceDetail(deviceMap, performanceStatisList, "sum_wifiUp");        vo.setFlowUp(wifiUpD);        List<Map> installRunTimeStatisList = taskService.getStepRuntimeStatis(taskCode, StepRequest.StepAction.installApp.name());        PerformanceOverview installD = getPerformanceDetail(deviceMap, installRunTimeStatisList, "avg_time");        vo.setInstall(installD);        List<Map> launchRunTimeStatisList = taskService.getStepRuntimeStatis(taskCode, StepRequest.StepAction.launch.name());        PerformanceOverview launchD = getPerformanceDetail(deviceMap, launchRunTimeStatisList, "avg_time");        vo.setStartUp(launchD);        return vo;    }    public PerformanceDeviceOverviewVO getPerformanceOverview(Task task, String deviceId) {        List<Map> performanceResult = taskService.getPerformanceDeviceStatis(task.getTaskCode(), deviceId);        List<Map> actionResult  = taskService.getSomeActionRuntime(task.getTaskCode(), deviceId);        PerformanceDeviceOverviewVO vo = new PerformanceDeviceOverviewVO();        Map<String, Object> result = null;        if(performanceResult.size() > 0){            result = performanceResult.get(0);        }        if (result != null) {            vo.setRam((Double) result.get("avg_mem"));            vo.setCpu((Double) result.get("avg_cpu"));            vo.setFps((Double) result.get("avg_fps"));            vo.setDown((Long) result.get("sum_wifiDown"));            vo.setUp((Long) result.get("sum_wifiUp"));        }        actionResult.forEach( a -> {            String action = (String) a.get("_id");            Double rt = (Double) a.get("avg_time");            if(StepRequest.StepAction.installApp.name().equals(action)) {                vo.setInstall(rt);            }            if(StepRequest.StepAction.launch.name().equals(action)) {                vo.setLaunch(rt);            }        });        return vo;    }    private PerformanceOverview getPerformanceDetail(Map<String,String> deviceMap, List<Map> avgList, String key) {        PerformanceOverview detail = new PerformanceOverview();        avgList.sort((Map o1, Map o2) -> {            try {                Double d1 = (Double) o1.getOrDefault(key, 0);                Double d2 = (Double) o2.getOrDefault(key, 0);                return d1.compareTo(d2);            }catch (ClassCastException e) {                Long d1 = (Long) o1.getOrDefault(key, 0);                Long d2 = (Long) o2.getOrDefault(key, 0);                return d1.compareTo(d2);            }        });        List<String> names = new ArrayList<>();        List<Double> values = new ArrayList<>();        for(Map m : avgList){            String deviceId = (String) m.getOrDefault("_id", "");            String name = deviceMap.get(deviceId);            names.add(name);            try {                Double d = (Double) m.getOrDefault(key, 0);                BigDecimal bg = new BigDecimal(d);                double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();                values.add(f1);            }catch (ClassCastException e) {                Long d = (Long) m.getOrDefault(key, 0);                values.add(d * 1.0);            }        }        detail.setName(names);        detail.setValue(values);        OptionalDouble avg = values.stream().mapToDouble(v -> v).average();        if (avg.isPresent()) {            detail.setOverview(doubleTwoPointFormat.format(avg.getAsDouble()));        }else{            log.error("average wasn't calculated");            detail.setOverview("0");        }        return detail;    }    /**     *@Description: 性能详情     *@Param: [task]     *@Return: com.testwa.distest.server.web.task.vo.echart.ReportEchartTimeVO     *@Author: wen     *@Date: 2018/5/24     */    public ReportPerformanceDetailVO getPerformanceDetail(Task task) {        ReportPerformanceDetailVO vo = new ReportPerformanceDetailVO();        Map<String, String> deviceMap = taskService.getDeviceNameMap(task.getDevices());        EchartLineCollection cpuLineList = new EchartLineCollection();        EchartLineCollection ramLineList = new EchartLineCollection();        EchartLineCollection fpsLineList = new EchartLineCollection();        task.getDevices().forEach(d -> {            List<Performance> performanceList = performanceService.findBy(task.getTaskCode(), d.getDeviceId());            EchartLine cpuLine = new EchartLine();            EchartLine ramLine = new EchartLine();            EchartLine fpsLine = new EchartLine();            performanceList.forEach( p -> {                String time = TimeUtil.formatTimeStamp(p.getTimestamp());                String name = deviceMap.get(p.getDeviceId());                EchartLinePoint cpuPoint = new EchartLinePoint();                cpuPoint.setTime(time);                cpuPoint.setValue(p.getCpu());                cpuLine.addPoint(cpuPoint);                cpuLine.setName(name);                EchartLinePoint ramPoint = new EchartLinePoint();                ramPoint.setTime(time);                double ram = (p.getMem() * 1.0)/1024;                BigDecimal bg = new BigDecimal(ram);                ram = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();                ramPoint.setValue(ram);                ramLine.addPoint(ramPoint);                ramLine.setName(name);                EchartLinePoint fpsPoint = new EchartLinePoint();                fpsPoint.setTime(time);                fpsPoint.setValue(p.getFps());                fpsLine.addPoint(fpsPoint);                fpsLine.setName(name);            });            cpuLineList.addLine(cpuLine);            ramLineList.addLine(ramLine);            fpsLineList.addLine(fpsLine);        });        vo.setCpu(cpuLineList);        vo.setRam(ramLineList);        vo.setFps(fpsLineList);        return vo;    }    /**     *@Description: 测试性能综合     *@Param: [task]     *@Return: com.testwa.distest.server.web.task.vo.ReportPerformanceSummaryVO     *@Author: wen     *@Date: 2018/5/24     */    public ReportPerformanceSummaryVO getPerformanceSummary(Task task) {        Long taskCode = task.getTaskCode();        List<Device> deviceList = task.getDevices();        Map<String, String> deviceMap = taskService.getDeviceNameMap(deviceList);        ReportPerformanceSummaryVO vo = new ReportPerformanceSummaryVO();        // install        List<Map> installRunTimeStatisList = taskService.getStepRuntimeStatis(taskCode, StepRequest.StepAction.installApp.name());        ReportPerformanceSummary installSummary = new ReportPerformanceSummary();        // 饼图        EchartPie pie = getEchartPie(installRunTimeStatisList, "avg_time", "s");        // 柱形图        EchartBar bar = getEchartBar(installRunTimeStatisList, deviceMap, "avg_time");        installSummary.setDistribution(pie);        installSummary.setWorst(bar);        vo.setInstall(installSummary);        // launch        List<Map> launchRunTimeStatisList = taskService.getStepRuntimeStatis(taskCode, StepRequest.StepAction.launch.name());        ReportPerformanceSummary launchSummary = new ReportPerformanceSummary();        // 饼图        EchartPie pie1 = getEchartPie(launchRunTimeStatisList, "avg_time", "s");        // 柱形图        EchartBar bar1 = getEchartBar(launchRunTimeStatisList, deviceMap, "avg_time");        launchSummary.setDistribution(pie1);        launchSummary.setWorst(bar1);        vo.setLaunch(launchSummary);        // ram        List<Map> ramAvgList = taskService.getPerformanceMemAvg(taskCode);        // 饼图        EchartPie pie2 = getEchartPie(ramAvgList, "avg_value", "M");        // 柱形图        EchartBar bar2 = getEchartBar(ramAvgList, deviceMap, "avg_value");        ReportPerformanceSummary ramSummary = new ReportPerformanceSummary();        ramSummary.setDistribution(pie2);        ramSummary.setWorst(bar2);        vo.setRam(ramSummary);        // cpu        List<Map> cpuAvgList = taskService.getPerformanceCPUAvg(taskCode);        // 饼图        EchartPie pie3 = getEchartPie(cpuAvgList, "avg_value", "%");        // 柱形图        EchartBar bar3 = getEchartBar(cpuAvgList, deviceMap, "avg_value");        ReportPerformanceSummary cpuSummary = new ReportPerformanceSummary();        cpuSummary.setDistribution(pie3);        cpuSummary.setWorst(bar3);        vo.setCpu(cpuSummary);        return vo;    }    private EchartPie getEchartPie(List<Map> deviceAvgValueList, String key, String unit) {        int size = deviceAvgValueList.size();        List<Double> runTimeList = new ArrayList<>();        for(Map m : deviceAvgValueList) {            Double d = (Double) m.get(key);            BigDecimal bgtemp = new BigDecimal(d);            runTimeList.add(bgtemp.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());        }        runTimeList = runTimeList.stream().sorted((o1, o2) -> (int)(o1 - o2)).collect(Collectors.toList());        EchartPie pie = new EchartPie();        if(runTimeList.size() > 0) {            Double min = runTimeList.get(0);            Double max = runTimeList.get(size - 1);            BigDecimal bg = new BigDecimal((max - min) * 1.0 / 5);            double diff = bg.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();            BigDecimal bg2 = new BigDecimal(runTimeList.get(0));            double lastvalue = bg2.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();            if( diff > 0) {                for (int i=0;i<5;i++){                    BigDecimal bg3 = new BigDecimal(lastvalue + diff);                    double thisvalue = bg3.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();                    String pointName = String.format("%s%s - %s%s", doubleTwoPointFormat.format(lastvalue), unit, doubleTwoPointFormat.format(thisvalue), unit);                    Integer pointValue = 0;                    for (Double r : runTimeList) {                        if(i == 4){                            if(lastvalue <= r && r <= thisvalue) {                                pointValue++;                            }                        }else{                            if(lastvalue <= r && r < thisvalue) {                                pointValue++;                            }                        }                    }                    lastvalue = thisvalue;                    EchartPiePoint point = new EchartPiePoint();                    point.setName(pointName);                    point.setValue(pointValue);                    pie.addPoint(point);                }            }else{                String pointName = String.format("%s%s - %s%s", doubleTwoPointFormat.format(lastvalue), unit, doubleTwoPointFormat.format(lastvalue), unit);                pie.addPoint(new EchartPiePoint(pointName, size));            }        }        return pie;    }    private EchartBar getEchartBar(List<Map> deviceAvgValueList, Map<String, String> deviceMap, String key) {        int size = deviceAvgValueList.size();        List<Map> endFive;        if(size >= 5){            endFive = deviceAvgValueList.subList(size-5, size);        }else{            endFive = deviceAvgValueList;        }        EchartBar bar = new EchartBar();        for(Map m : endFive) {            String deviceId = (String) m.getOrDefault("_id", "");            double value = (double) m.getOrDefault(key, 0);            bar.add(deviceMap.get(deviceId), doubleTwoPointFormat.format(value));        }        return bar;    }    /**     *@Description: 兼容测试流量     *@Param: [task, deviceId]     *@Return: com.testwa.distest.server.web.task.vo.echart.EchartDoubleLine     *@Author: wen     *@Date: 2018/5/24     */    public EchartDoubleLine getPerformanceFlowSummary(Task task, String deviceId) {        EchartDoubleLine line = new EchartDoubleLine();        List<Performance> performanceList = performanceService.findBy(task.getTaskCode(), deviceId);        performanceList.forEach( p -> {            String time = TimeUtil.formatTimeStamp(p.getTimestamp());            line.add(time, p.getGprsUp() + p.getWifiUp(), p.getWifiDown() + p.getGprsDown());        });        return line;    }}