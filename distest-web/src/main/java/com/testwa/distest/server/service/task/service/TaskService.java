package com.testwa.distest.server.service.task.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mongo.model.ExecutorLogInfo;
import com.testwa.distest.server.mongo.model.Performance;
import com.testwa.distest.server.mongo.model.ProcedureInfo;
import com.testwa.distest.server.mongo.model.ProcedureStatis;
import com.testwa.distest.server.mongo.repository.ExecutorLogInfoRepository;
import com.testwa.distest.server.mongo.repository.PerformanceRepository;
import com.testwa.distest.server.mongo.repository.ProcedureInfoRepository;
import com.testwa.distest.server.mongo.repository.ProcedureStatisRepository;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.task.dao.ITaskDAO;
import com.testwa.distest.server.service.task.dto.TaskDeviceStatusStatis;
import com.testwa.distest.server.service.task.form.ScriptListForm;
import com.testwa.distest.server.service.task.form.TaskListForm;
import com.testwa.distest.server.web.task.vo.*;
import com.testwa.distest.server.web.task.vo.echart.EchartDoubleLine;
import com.testwa.distest.server.web.task.vo.echart.EchartLine;
import com.testwa.distest.server.web.task.vo.echart.EchartLinePoint;
import com.testwa.distest.server.web.task.vo.echart.EchartLineCollection;
import io.rpc.testwa.task.StepRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

/**
 * Created by wen on 24/10/2017.
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class TaskService {

    @Autowired
    private ITaskDAO taskDAO;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProcedureStatisRepository procedureStatisRepository;
    @Autowired
    private ProcedureInfoRepository procedureInfoRepository;
    @Autowired
    private PerformanceRepository performanceRepository;
    @Autowired
    private ExecutorLogInfoRepository executorLogInfoRepository;
    @Autowired
    private TaskDeviceService taskDeviceService;
    @Autowired
    private MongoOperations mongoTemplate;


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long save(Task entity) {
        return taskDAO.insert(entity);
    }

    public Task findOne(Long entityId) {
        return taskDAO.findOne(entityId);
    }

    public List<Task> findAll(List<Long> entityIds) {
        return taskDAO.findAll(entityIds);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void update(Task entity) {
        taskDAO.update(entity);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteTask(List<Long> entityIds) {
        taskDAO.delete(entityIds);
        entityIds.forEach( id -> {

            taskDeviceService.deleteTaskDeviceByTaskId(id);
            List<ProcedureInfo> infos = procedureInfoRepository.findByExecutionTaskIdOrderByTimestampAsc(id);
            procedureInfoRepository.delete(infos);
        });
    }

    public Map<String, Object> statis(Task task) {
        ProcedureStatis ps = procedureStatisRepository.findByExeId(task.getId());
        Map<String, Object> result = new HashMap<>();
        // taskType
        result.put("taskType", task.getTaskType());
        // taskStatus

        List<TaskDeviceStatusStatis> tds = taskDeviceService.countTaskDeviceStatus(task.getId());
        task.setDeviceStatusStatis(tds);
        result.put("taskStatus", task.getDeviceStatusStatis());
        // app 基本情况
        result.put("appStaty", task.getApp());
        // 设备基本情况
        result.put("devInfo", task.getDevices());

        if(ps != null) {

            Map<String, Device> devInfo = new HashMap<>();
            // 设备基本情况
            task.getDevices().forEach(device -> {
                devInfo.put(device.getDeviceId(), device);
            });

            List<Map> statusScript = ps.getStatusScriptInfo();

            // 设备脚本执行情况，app信息可以从app基本情况获得
            /*
             {
             "dto": "TaskScene 4",
             "brand": "lantern",
             "state": 128 ,
             "successNum": 23,
             "failedNum": 25,
             "scriptName": "已执行" ,
             "appName": "查看",
             "appVersion": 123
             }
             */
            List<Map> scriptStaty = new ArrayList<>();
            statusScript.forEach(s -> {
                String deviceId = (String) s.get("deviceId");
                Map<String, Object> subInfo = new HashMap<>();
                subInfo.put("deviceId", deviceId);
                subInfo.put("successNum", s.get("success"));
                subInfo.put("failedNum", s.get("fail"));
                subInfo.put("total", ps.getScriptNum());
                Device d = devInfo.get(deviceId);
                if (d != null) {
                    subInfo.put("dto", d.getModel());
                    subInfo.put("brand", d.getBrand());
                    scriptStaty.add(subInfo);
                }
            });
            result.put("scriptStaty", scriptStaty);
        }
        return result;
    }

    /**
     * 获得当前登录用户可见的所有任务列表
     * @param pageForm
     * @return
     */
    public PageResult<Task> findPageForCurrentUser(TaskListForm pageForm) {

        Map<String, Object> params = buildQueryParams(pageForm);
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<Task> entityList = taskDAO.findByFromProject(params);
        entityList.forEach(entity -> {
            List<TaskDeviceStatusStatis> tds = taskDeviceService.countTaskDeviceStatus(entity.getId());
            entity.setDeviceStatusStatis(tds);
        });
        PageInfo<Task> info = new PageInfo(entityList);
        PageResult<Task> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    private Map<String, Object> buildQueryParams(TaskListForm queryForm) {
        List<Project> projects = projectService.findAllByUserList(getCurrentUsername());
        Map<String, Object> params = new HashMap<>();
        if(queryForm.getProjectId() != null){
            params.put("projectId", queryForm.getProjectId());
        }
        if(StringUtils.isNotEmpty(queryForm.getTaskName())){
            params.put("taskName", queryForm.getTaskName());
        }
        if(projects != null){
            params.put("projects", projects);
        }
        return params;
    }


    /**
     * 获得当前登录用户可见的所有任务列表
     * @param pageForm
     * @return
     */
    public PageResult<Task> findPageForCreateUser(TaskListForm pageForm, Long userId) {

        Map<String, Object> params = buildQueryParams(pageForm);
        params.put("createBy", userId);
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        PageHelper.orderBy(pageForm.getOrderBy() + " " + pageForm.getOrder());
        List<Task> entityList = taskDAO.findByFromProject(params);
        entityList.forEach(entity -> {
            List<TaskDeviceStatusStatis> tds = taskDeviceService.countTaskDeviceStatus(entity.getId());
            entity.setDeviceStatusStatis(tds);
        });
        PageInfo<Task> info = new PageInfo(entityList);
        PageResult<Task> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<Script> findScriptListInTask(ScriptListForm form) {
        Task task = taskDAO.findOne(form.getTaskId());
        return task.getScriptList();
    }

    public TaskProgressVO getProgress(Long taskId) {
        Task task = taskDAO.findOne(taskId);
        List<Device> deviceList = task.getDevices();
        Map<String, List<String>> dateMap = new HashMap<>();
        Map<String, String> deviceMap = new HashMap<>();
        deviceList.forEach( d -> {
            List<String> dataList = new ArrayList<>();
            DateTime dt = new DateTime(task.getCreateTime());
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            dataList.add(fmt.print(dt));
            dateMap.put(d.getDeviceId(), dataList);
            deviceMap.put(d.getDeviceId(), d.getBrand() + " " + d.getModel());
        });

        List<Integer> orders = Arrays.asList(0,1,2,3,6);

        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("taskId").is(taskId),
                Criteria.where("actionOrder").in(orders));
        Sort sort = new Sort(Sort.Direction.ASC, "actionOrder","timestamp");
        Query query = new Query();
        query.addCriteria(criatira);
        query.with(sort);
        List<ExecutorLogInfo> loginfos = executorLogInfoRepository.find(query);

        for(ExecutorLogInfo l : loginfos) {
            List<String> datas = dateMap.get(l.getDeviceId());
            if(l.getActionOrder() != 6 && "start".equals(l.getFlag())){
                datas.add(TimeUtil.formatTimeStamp(l.getTimestamp()));
            }
            if(l.getActionOrder() == 6){
                datas.add(TimeUtil.formatTimeStamp(l.getTimestamp()));
            }
        }
        List<TaskProgressLine> lines = new ArrayList<>();
        dateMap.forEach( (k, v) -> {
            TaskProgressLine line = new TaskProgressLine();
            line.setName(deviceMap.get(k));
            line.setData(v);
            lines.add(line);
        });
        TaskProgressVO vo = new TaskProgressVO();
        vo.setLineList(lines);
        vo.setDeviceNameList(new ArrayList<>(deviceMap.values()));
        return vo;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void updateEndTime(Long taskId) {
        Date endTime = new Date();
        taskDAO.updateEndTime(taskId, endTime);
    }

    public TaskOverviewVO getOverview(Long taskId) {
        TaskOverviewVO vo = new TaskOverviewVO();
        Task task = taskDAO.findOne(taskId);
        if(task.getEndTime() == null) {
            vo.setStatus(TaskOverviewVO.TaskStatus.RUNNING);
            return vo;
        }
        vo.setStatus(TaskOverviewVO.TaskStatus.COMPLETE);
        int deviceSize = task.getDevices().size();
        List<ProcedureInfo> installList = getAllInstallSuccessProcedure(taskId);
        double install = (installList.size() / deviceSize) * 100;
        BigDecimal bg = new BigDecimal(install);
        install = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        List<ProcedureInfo> lanucherList = getAllLanucherSuccessProcedure(taskId);
        double lanucher = (lanucherList.size() / deviceSize) * 100;
        bg = new BigDecimal(lanucher);
        lanucher = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        List<ProcedureInfo> uninstallList = getAllUninstallSuccessProcedure(taskId);
        double uninstall = (uninstallList.size() / deviceSize) * 100;
        bg = new BigDecimal(uninstall);
        uninstall = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();


        List<Map> statusStatis = getStatusStatisByDeviceId(taskId);
        int success = 0;
        int fail = 0;
        for(Map m : statusStatis){
            int status = (int) m.getOrDefault("_id", 0);
            if(status == 0){
                success = (int) m.getOrDefault("count", 0);
            }
            if(status == 1){
                fail = (int) m.getOrDefault("count", 0);
            }
        }
        int conclusion = ( success + fail ) == 0 ? 0 : (success / (success + fail)) * 100;
        vo.setConclusion(conclusion + "%");
        vo.setValue(Arrays.asList(install, lanucher, uninstall));
        return vo;
    }

    private List<Map> getStatusStatisByDeviceId(Long taskId) {

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(taskId)),
                Aggregation.group( "status").count().as("count")
        );
        AggregationResults<BasicDBObject> outputType = mongoTemplate.aggregate(agg, "t_procedure_info", BasicDBObject.class);
        List<Map> result = new ArrayList<>();
        for (Iterator<BasicDBObject> iterator = outputType.iterator(); iterator.hasNext();) {
            DBObject obj =iterator.next();
            result.add(obj.toMap());
        }
        return result;
    }

    public List<ProcedureInfo> getAllInstallSuccessProcedure(Long taskId) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("executionTaskId").is(taskId),
                Criteria.where("status").is(0),
                Criteria.where("command.action").is("安装应用"));

        return procedureInfoRepository.find(new Query(criatira));
    }

    public List<ProcedureInfo> getAllLanucherSuccessProcedure(Long taskId) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("executionTaskId").is(taskId),
                Criteria.where("status").is(0),
                Criteria.where("command.action").is("启动应用"));
        return procedureInfoRepository.find(new Query(criatira));
    }

    public List<ProcedureInfo> getAllUninstallSuccessProcedure(Long taskId) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("executionTaskId").is(taskId),
                Criteria.where("status").is(0),
                Criteria.where("command.action").is("卸载应用"));
        return procedureInfoRepository.find(new Query(criatira));
    }

    public TaskDeviceFinishStatisVO getFinishStatisVO(Long taskId) {
        TaskDeviceFinishStatisVO vo = new TaskDeviceFinishStatisVO();
        List<TaskDevice> taskDeviceList = taskDeviceService.findByTaskId(taskId);
        if(taskDeviceList.size() == 0){
            return vo;
        }
        List<TaskDeviceFinishGraph> graph = new ArrayList<>();
        int running = 0;
        int completed = 0;
        int extremely = 0;
        int cancel = 0;
        for(TaskDevice taskDevice : taskDeviceList) {
            switch (taskDevice.getStatus()){
                case CANCEL:
                    cancel++;
                    break;
                case RUNNING:
                    running++;
                    break;
                case COMPLETE:
                    completed++;
                    break;
                case ERROR:
                    extremely++;
                    break;
            }
        }
        int progress = (1 - (running/taskDeviceList.size())) * 100;
        TaskDeviceFinishStatistics statistics = new TaskDeviceFinishStatistics();
        statistics.setCancel(cancel);
        statistics.setCompleted(completed);
        statistics.setExtremely(extremely);
        statistics.setRunning(running);
        statistics.setProgress(progress + "%");

        if(running > 0) {
            TaskDeviceFinishGraph subGraph = new TaskDeviceFinishGraph();
            subGraph.setName("运行中");
            subGraph.setValue(running);
            graph.add(subGraph);
        }
        if(completed > 0) {
            TaskDeviceFinishGraph subGraph = new TaskDeviceFinishGraph();
            subGraph.setName("完成");
            subGraph.setValue(completed);
            graph.add(subGraph);
        }
        if(extremely > 0) {
            TaskDeviceFinishGraph subGraph = new TaskDeviceFinishGraph();
            subGraph.setName("异常");
            subGraph.setValue(extremely);
            graph.add(subGraph);
        }
        if(cancel > 0) {
            TaskDeviceFinishGraph subGraph = new TaskDeviceFinishGraph();
            subGraph.setName("取消");
            subGraph.setValue(cancel);
            graph.add(subGraph);
        }
        vo.setGraph(graph);
        vo.setStatistics(statistics);
        return vo;
    }

    /**
     *@Description: 回归测试性能统计
     *@Param: [task]
     *@Return: com.testwa.distest.server.web.task.vo.PerformanceOverviewVO
     *@Author: wen
     *@Date: 2018/5/23
     */
    public PerformanceOverviewVO getHGPerformanceOverview(Task task) {
        PerformanceOverviewVO vo = new PerformanceOverviewVO();
        Long taskId = task.getId();
        List<Device> deviceList = task.getDevices();
        Map<String, String> deviceMap = getDeviceNameMap(deviceList);

        List<Map> memoryavg = getMemoryAvg(taskId);
        PerformanceOverview memoryD = getPerformanceDetail(deviceMap, memoryavg);
        vo.setRam(memoryD);

        List<Map> cpuavg = getCpuAvg(taskId);
        PerformanceOverview cpuD = getPerformanceDetail(deviceMap, cpuavg);
        vo.setCpu(cpuD);

        List<Map> install = getInstallTime(taskId);
        PerformanceOverview installD = getPerformanceDetail(deviceMap, install);
        vo.setInstall(installD);

        List<Map> startup = getStartUpTime(taskId);
        PerformanceOverview startupD = getPerformanceDetail(deviceMap, startup);
        vo.setStartUp(startupD);

        return vo;
    }

    /**
     *@Description: key= deviceId  value= modelName
     *@Param: [deviceList]
     *@Return: java.util.Map<java.lang.String,java.lang.String>
     *@Author: wen
     *@Date: 2018/5/24
     */
    private Map<String, String> getDeviceNameMap(List<Device> deviceList) {
        Map<String, String> deviceMap = new HashMap<>();
        deviceList.forEach( d -> {
            deviceMap.put(d.getDeviceId(), d.getModel().contains(d.getBrand()) ? d.getModel() : d.getBrand() + " " + d.getModel());
        });
        return deviceMap;
    }

    private PerformanceOverview getPerformanceDetail(Map<String, String> deviceMap, List<Map> avgList) {
        PerformanceOverview detail = new PerformanceOverview();
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for(Map m : avgList){
            String deviceId = (String) m.getOrDefault("_id", "");
            String name = deviceMap.get(deviceId);
            names.add(name);
            Double d = (Double) m.getOrDefault("value", 0);
            BigDecimal bg = new BigDecimal(d);
            double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            values.add(f1);
        }
        detail.setName(names);
        detail.setValue(values);
        OptionalDouble avg = values.stream().mapToDouble(v -> v).average();
        if (avg.isPresent()) {
            detail.setOverview(String.format("%.2f", avg.getAsDouble()));
        }else{
           log.error("average wasn't calculated");
            detail.setOverview("0");
        }
        return detail;
    }


    private List<Map> getStartUpTime(Long taskId) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("executionTaskId").is(taskId),
                Criteria.where("command.action").is("启动应用"));
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criatira),
                Aggregation.project("runtime", "deviceId").andExpression("runtime/1000").as("second"),
                Aggregation.group("deviceId").avg("second").as("value")
        );
        return getResult(agg, "t_procedure_info");
    }

    private List<Map> getInstallTime(Long taskId) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("executionTaskId").is(taskId),
                Criteria.where("command.action").is("安装应用"));
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criatira),
                Aggregation.project("runtime", "deviceId").andExpression("runtime/1000").as("second"),
                Aggregation.group("deviceId").avg("second").as("value")
        );
        return getResult(agg, "t_procedure_info");
    }

    private List<Map> getMemoryAvg(Long taskId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(taskId)),
                Aggregation.project("memory", "deviceId").andExpression("memory/1024").as("m"),
                Aggregation.group("deviceId").avg("m").as("value")
        );
        return getResult(agg, "t_procedure_info");
    }

    private List<Map> getCpuAvg(Long taskId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(taskId)),
                Aggregation.group("deviceId").avg("cpurate").as("value")
        );
        return getResult(agg, "t_procedure_info");
    }

    private List<Map> getResult(Aggregation agg, String tableName) {
        AggregationResults<BasicDBObject> outputType = mongoTemplate.aggregate(agg, tableName, BasicDBObject.class);
        List<Map> result = new ArrayList<>();
        for (Iterator<BasicDBObject> iterator = outputType.iterator(); iterator.hasNext();) {
            DBObject obj =iterator.next();
            result.add(obj.toMap());
        }
        return result;
    }

    /**
     *@Description: 兼容测试性能统计
     *@Param: [task]
     *@Return: com.testwa.distest.server.web.task.vo.PerformanceOverviewVO
     *@Author: wen
     *@Date: 2018/5/23
     */
    public PerformanceOverviewVO getJRPerformanceOverview(Task task) {
        PerformanceOverviewVO vo = new PerformanceOverviewVO();
        Long taskId = task.getId();
        List<Device> deviceList = task.getDevices();
        Map<String, String> deviceMap = getDeviceNameMap(deviceList);

        List<Map> performanceStatisList = getPerformanceStatis(taskId);
        PerformanceOverview memoryD = getPerformanceDetail(deviceMap, performanceStatisList, "avg_mem");
        vo.setRam(memoryD);
        PerformanceOverview cpuD = getPerformanceDetail(deviceMap, performanceStatisList, "avg_cpu");
        vo.setCpu(cpuD);
        PerformanceOverview fpsD = getPerformanceDetail(deviceMap, performanceStatisList, "avg_fps");
        vo.setFps(fpsD);
        PerformanceOverview wifiDownD = getPerformanceDetail(deviceMap, performanceStatisList, "sum_wifiDown");
        vo.setFlowDown(wifiDownD);
        PerformanceOverview wifiUpD = getPerformanceDetail(deviceMap, performanceStatisList, "sum_wifiUp");
        vo.setFlowUp(wifiUpD);

        List<Map> installRunTimeStatisList = getStepRuntimeStatis(taskId, StepRequest.StepAction.installApp.name());
        PerformanceOverview installD = getPerformanceDetail(deviceMap, installRunTimeStatisList, "avg_time");
        vo.setInstall(installD);

        List<Map> launchRunTimeStatisList = getStepRuntimeStatis(taskId, StepRequest.StepAction.launch.name());
        PerformanceOverview launchD = getPerformanceDetail(deviceMap, launchRunTimeStatisList, "avg_time");
        vo.setStartUp(launchD);

        return vo;
    }

    private PerformanceOverview getPerformanceDetail(Map<String,String> deviceMap, List<Map> avgList, String key) {
        PerformanceOverview detail = new PerformanceOverview();
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for(Map m : avgList){
            String deviceId = (String) m.getOrDefault("_id", "");
            String name = deviceMap.get(deviceId);
            names.add(name);
            try {
                Double d = (Double) m.getOrDefault(key, 0);
                BigDecimal bg = new BigDecimal(d);
                double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                values.add(f1);
            }catch (ClassCastException e) {
                Long d = (Long) m.getOrDefault(key, 0);
                values.add(d * 1.0);
            }
        }
        detail.setName(names);
        detail.setValue(values);
        OptionalDouble avg = values.stream().mapToDouble(v -> v).average();
        if (avg.isPresent()) {
            detail.setOverview(String.format("%.2f", avg.getAsDouble()));
        }else{
            log.error("average wasn't calculated");
            detail.setOverview("0");
        }
        return detail;
    }

    /**
     *@Description: 获得性能指标统计数据
     * ﻿db.t_performance.aggregate(
     *   [
     *    { $match: { taskId: 164 } },
     *    { $project: {"deviceId": 1, "mem": 1, "cpu": 1, "fps": 1, "wifiDown": 1, "wifiUp": 1} },
     *    { $group: { _id: "$deviceId", avg_mem: {$avg: "$mem"}, avg_cpu: {$avg: "$cpu"}, avg_fps: {$avg: "$fps"}, avg_wifiDown: {$avg: "$wifiDown"}}}
     *  ]
     * )
     *@Param: [taskId]
     *@Return: java.util.List<java.util.Map>
     *@Author: wen
     *@Date: 2018/5/23
     */
    private List<Map> getPerformanceStatis(Long taskId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskId").is(taskId)),
                Aggregation.project("deviceId","mem","cpu","fps","wifiDown","wifiUp")
                        .andExpression("mem/1024").as("ram")
                        .andExpression("wifiDown*01").as("down")
                        .andExpression("wifiUp*01").as("up"),
                Aggregation.group("deviceId")
                        .avg("ram").as("avg_mem")
                        .avg("cpu").as("avg_cpu")
                        .avg("fps").as("avg_fps")
                        .sum("down").as("sum_wifiDown")
                        .sum("up").as("sum_wifiUp")
        );
        return getResult(agg, "t_performance");
    }

    /**
     *@Description:  步骤时间统计
     * ﻿db.t_step.aggregate(
     *   [
     *    { $match: { taskId: 164, action: 'installApp'} },
     *    { $project: {"runtime": 1, "action": 1, "deviceId": 1} },
     *    { $group: { _id: "$deviceId", avg_time: {$avg: "$runtime"}}}
     *  ]
     * )
     *@Param: [taskId]
     *@Return: java.util.List<java.util.Map>  {'deviceId': xxxxx, 'avg_time': xxxx}
     *@Author: wen
     *@Date: 2018/5/23
     */
    private List<Map> getStepRuntimeStatis(Long taskId, String actionName) {
        List<String> actions = Arrays.asList(StepRequest.StepAction.installApp.name(), StepRequest.StepAction.launch.name(), StepRequest.StepAction.uninstallApp.name());
        if(!actions.contains(actionName)){
            return new ArrayList<>();
        }
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(Criteria.where("taskId").is(taskId).and("action").is(actionName))),
                Aggregation.project("runtime","action","deviceId")
                        .andExpression("runtime/1000").as("rt"),
                Aggregation.group("deviceId")
                        .avg("rt").as("avg_time")
        );
        return getResult(agg, "t_step");
    }


    /**
     *@Description: 回归测试性能详情
     *@Param: [task]
     *@Return: com.testwa.distest.server.web.task.vo.echart.ReportEchartTimeVO
     *@Author: wen
     *@Date: 2018/5/24
     */
    public ReportPerformanceDetailVO getHGPerformanceDetail(Task task) {
        ReportPerformanceDetailVO vo = new ReportPerformanceDetailVO();

        Map<String, String> deviceMap = getDeviceNameMap(task.getDevices());

        EchartLineCollection cpuLineList = new EchartLineCollection();
        EchartLineCollection ramLineList = new EchartLineCollection();
        EchartLineCollection fpsLineList = new EchartLineCollection();
        task.getDevices().forEach(d -> {
            List<ProcedureInfo> performanceList = procedureInfoRepository.findByExecutionTaskIdAndDeviceIdOrderByTimestampAsc(task.getId(), d.getDeviceId());
            EchartLine cpuLine = new EchartLine();
            EchartLine ramLine = new EchartLine();
            EchartLine fpsLine = new EchartLine();
            performanceList.forEach( p -> {
                String time = TimeUtil.formatTimeStamp(p.getTimestamp());
                String name = deviceMap.get(p.getDeviceId());
                EchartLinePoint cpuPoint = new EchartLinePoint();
                cpuPoint.setTime(time);
                cpuPoint.setValue(p.getCpurate());
                cpuLine.addPoint(cpuPoint);
                cpuLine.setName(name);

                EchartLinePoint ramPoint = new EchartLinePoint();
                ramPoint.setTime(time);
                ramPoint.setValue(p.getMemory());
                ramLine.addPoint(cpuPoint);
                ramLine.setName(name);
                // 回归测试还拿不到以下几项
//                ReportEchartTimePoint fpsPoint = new ReportEchartTimePoint();
//                fpsPoint.setTime(time);
//                fpsPoint.setValue(p.getFps());
//                fpsLine.addPoint(cpuPoint);
//                fpsLine.setName(name);
            });
            cpuLineList.addLine(cpuLine);
            ramLineList.addLine(ramLine);
            fpsLineList.addLine(fpsLine);
        });

        vo.setCpu(cpuLineList);
        vo.setRam(ramLineList);
        vo.setFps(fpsLineList);
        return vo;
    }

    /**
     *@Description: 兼容测试性能详情
     *@Param: [task]
     *@Return: com.testwa.distest.server.web.task.vo.echart.ReportEchartTimeVO
     *@Author: wen
     *@Date: 2018/5/24
     */
    public ReportPerformanceDetailVO getJRPerformanceDetail(Task task) {
        ReportPerformanceDetailVO vo = new ReportPerformanceDetailVO();

        Map<String, String> deviceMap = getDeviceNameMap(task.getDevices());

        EchartLineCollection cpuLineList = new EchartLineCollection();
        EchartLineCollection ramLineList = new EchartLineCollection();
        EchartLineCollection fpsLineList = new EchartLineCollection();
        task.getDevices().forEach(d -> {
            List<Performance> performanceList = performanceRepository.findByTaskIdAndDeviceIdOrderByTimestampAsc(task.getId(), d.getDeviceId());
            EchartLine cpuLine = new EchartLine();
            EchartLine ramLine = new EchartLine();
            EchartLine fpsLine = new EchartLine();
            performanceList.forEach( p -> {
                String time = TimeUtil.formatTimeStamp(p.getTimestamp());
                String name = deviceMap.get(p.getDeviceId());
                EchartLinePoint cpuPoint = new EchartLinePoint();
                cpuPoint.setTime(time);
                cpuPoint.setValue(p.getCpu());
                cpuLine.addPoint(cpuPoint);
                cpuLine.setName(name);

                EchartLinePoint ramPoint = new EchartLinePoint();
                ramPoint.setTime(time);
                ramPoint.setValue(p.getMem());
                ramLine.addPoint(ramPoint);
                ramLine.setName(name);

                EchartLinePoint fpsPoint = new EchartLinePoint();
                fpsPoint.setTime(time);
                fpsPoint.setValue(p.getFps());
                fpsLine.addPoint(fpsPoint);
                fpsLine.setName(name);

            });
            cpuLineList.addLine(cpuLine);
            ramLineList.addLine(ramLine);
            fpsLineList.addLine(fpsLine);
        });

        vo.setCpu(cpuLineList);
        vo.setRam(ramLineList);
        vo.setFps(fpsLineList);
        return vo;

    }

    /**
     *@Description: 回归测试性能综合
     *@Param: [task]
     *@Return: com.testwa.distest.server.web.task.vo.ReportPerformanceSummaryVO
     *@Author: wen
     *@Date: 2018/5/24
     */
    public ReportPerformanceSummaryVO getHGPerformanceSummary(Task task) {
        ReportPerformanceSummaryVO vo = new ReportPerformanceSummaryVO();
        // install
        // launch
        // cpu
        // ram
        return vo;
    }

    /**
     *@Description: 兼容测试性能综合
     *@Param: [task]
     *@Return: com.testwa.distest.server.web.task.vo.ReportPerformanceSummaryVO
     *@Author: wen
     *@Date: 2018/5/24
     */
    public ReportPerformanceSummaryVO getJRPerformanceSummary(Task task) {
        ReportPerformanceSummaryVO vo = new ReportPerformanceSummaryVO();
        // install
        List<Map> installRunTimeStatisList = getStepRuntimeStatis(task.getId(), StepRequest.StepAction.installApp.name());
        installRunTimeStatisList.forEach(m -> {

        });
        // launch
        List<Map> launchRunTimeStatisList = getStepRuntimeStatis(task.getId(), StepRequest.StepAction.launch.name());

        // cpu
        // ram
        return vo;
    }


    /**
     *@Description: 回归测试流量
     *@Param: [task, deviceId]
     *@Return: com.testwa.distest.server.web.task.vo.echart.EchartDoubleLine
     *@Author: wen
     *@Date: 2018/5/24
     */
    public EchartDoubleLine getHGPerformanceFlowSummary(Task task, String deviceId) {
        EchartDoubleLine line = new EchartDoubleLine();

        return line;
    }

    /**
     *@Description: 兼容测试流量
     *@Param: [task, deviceId]
     *@Return: com.testwa.distest.server.web.task.vo.echart.EchartDoubleLine
     *@Author: wen
     *@Date: 2018/5/24
     */
    public EchartDoubleLine getJRPerformanceFlowSummary(Task task, String deviceId) {
        EchartDoubleLine line = new EchartDoubleLine();

        List<Performance> performanceList = performanceRepository.findByTaskIdAndDeviceIdOrderByTimestampAsc(task.getId(), deviceId);
        performanceList.forEach( p -> {
            String time = TimeUtil.formatTimeStamp(p.getTimestamp());
            line.add(time, p.getGprsUp() + p.getWifiUp(), p.getWifiDown() + p.getGprsDown());
        });

        return line;
    }
}
