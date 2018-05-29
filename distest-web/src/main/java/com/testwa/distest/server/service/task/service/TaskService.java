package com.testwa.distest.server.service.task.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mongo.model.*;
import com.testwa.distest.server.mongo.repository.*;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.task.dao.ITaskDAO;
import com.testwa.distest.server.service.task.dto.TaskDeviceStatusStatis;
import com.testwa.distest.server.service.task.form.ScriptListForm;
import com.testwa.distest.server.service.task.form.TaskListForm;
import io.rpc.testwa.task.StepRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private AppiumRunningLogRepository procedureInfoRepository;
    @Autowired
    private StepRepository stepRepository;
    @Autowired
    private PerformanceRepository performanceRepository;
    @Autowired
    private MethodRunningLogRepository executorLogInfoRepository;
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
            List<AppiumRunningLog> infos = procedureInfoRepository.findByExecutionTaskIdOrderByTimestampAsc(id);
            procedureInfoRepository.delete(infos);
        });
    }

    public Map<String, Object> statis(Task task) {
        Map<String, Object> result = new HashMap<>();
        // taskType
        result.put("taskType", task.getTaskType());
        // taskStatus
        List<TaskDeviceStatusStatis> tds = taskDeviceService.countTaskDeviceStatus(task.getId());
        task.setDeviceStatusStatis(tds);
        result.put("taskStatus", task.getDeviceStatusStatis());
        // app 基本情况
        result.put("appStaty", task.getApp());
        // script 基本情况
        result.put("scriptStaty", task.getScriptList());
        // 设备基本情况
        result.put("devInfo", task.getDevices());

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


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void updateEndTime(Long taskId) {
        Date endTime = new Date();
        taskDAO.updateEndTime(taskId, endTime);
    }


    public List<Map> getAppiumRunningLogStatisByTaskId(Long taskId) {

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(taskId)),
                Aggregation.group( "status").count().as("count")
        );

        return getResult(agg, AppiumRunningLog.getCollectionName());
    }

    public List<AppiumRunningLog> getAllInstallSuccessProcedure(Long taskId) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("executionTaskId").is(taskId),
                Criteria.where("status").is(0),
                Criteria.where("command.action").is("安装应用"));

        return procedureInfoRepository.find(new Query(criatira));
    }

    public List<AppiumRunningLog> getAllLanucherSuccessProcedure(Long taskId) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("executionTaskId").is(taskId),
                Criteria.where("status").is(0),
                Criteria.where("command.action").is("启动应用"));
        return procedureInfoRepository.find(new Query(criatira));
    }

    public List<AppiumRunningLog> getAllUninstallSuccessProcedure(Long taskId) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("executionTaskId").is(taskId),
                Criteria.where("status").is(0),
                Criteria.where("command.action").is("卸载应用"));
        return procedureInfoRepository.find(new Query(criatira));
    }

    public List<Step> getSuccessStep(Long taskId, StepRequest.StepAction uninstallApp) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("taskId").is(taskId),
                Criteria.where("status").is(StepRequest.StepStatus.SUCCESS.getNumber()),
                Criteria.where("action").is(uninstallApp.name()));

        return stepRepository.find(new Query(criatira));
    }

    public List<Map> getStepStatusStatis(Long taskId) {

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskId").is(taskId)),
                Aggregation.group( "status").count().as("count")
        );
        return getResult(agg, Step.getCollectionName());
    }


    /**
     *@Description: key= deviceId  value= modelName
     *@Param: [deviceList]
     *@Return: java.util.Map<java.lang.String,java.lang.String>
     *@Author: wen
     *@Date: 2018/5/24
     */
    public Map<String, String> getDeviceNameMap(List<Device> deviceList) {
        Map<String, String> deviceMap = new HashMap<>();
        deviceList.forEach( d -> {
            deviceMap.put(d.getDeviceId(), d.getModel().contains(d.getBrand()) ? d.getModel() : d.getBrand() + " " + d.getModel());
        });
        return deviceMap;
    }

    public List<Map> getStartUpTime(Long taskId) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("executionTaskId").is(taskId),
                Criteria.where("command.action").is("启动应用"));
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criatira),
                Aggregation.project("runtime", "deviceId").andExpression("runtime/1000").as("second"),
                Aggregation.group("deviceId").avg("second").as("value")
        );
        return getResult(agg, AppiumRunningLog.getCollectionName());
    }

    public List<Map> getInstallTime(Long taskId) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("executionTaskId").is(taskId),
                Criteria.where("command.action").is("安装应用"));
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criatira),
                Aggregation.project("runtime", "deviceId").andExpression("runtime/1000").as("second"),
                Aggregation.group("deviceId").avg("second").as("value")
        );
        return getResult(agg, AppiumRunningLog.getCollectionName());
    }

    public List<Map> getMemoryAvg(Long taskId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(taskId)),
                Aggregation.project("memory", "deviceId").andExpression("memory/1024").as("m"),
                Aggregation.group("deviceId").avg("m").as("value")
        );
        return getResult(agg, AppiumRunningLog.getCollectionName());
    }

    public List<Map> getCpuAvg(Long taskId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(taskId)),
                Aggregation.group("deviceId").avg("cpurate").as("value")
        );
        return getResult(agg, AppiumRunningLog.getCollectionName());
    }

    public List<Map> getResult(Aggregation agg, String tableName) {
        AggregationResults<BasicDBObject> outputType = mongoTemplate.aggregate(agg, tableName, BasicDBObject.class);
        List<Map> result = new ArrayList<>();
        for (Iterator<BasicDBObject> iterator = outputType.iterator(); iterator.hasNext();) {
            DBObject obj =iterator.next();
            result.add(obj.toMap());
        }
        return result;
    }

    public List<Map> getSomeActionRuntime(Long taskId, String deviceId) {
        List<String> actions = Arrays.asList(StepRequest.StepAction.installApp.name(), StepRequest.StepAction.launch.name(), StepRequest.StepAction.uninstallApp.name());

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(Criteria.where("taskId").is(taskId).and("action").in(actions).and("deviceId").is(deviceId))),
                Aggregation.project("runtime","action")
                        .andExpression("runtime/1000").as("rt"),
                Aggregation.group("action")
                        .avg("rt").as("avg_time"),
                Aggregation.sort(Sort.Direction.ASC, "avg_time")
        );
        return getResult(agg, Step.getCollectionName());
    }

    /**
     *@Description: 获得性能指标统计数据
     * ﻿db.t_performance.aggregate(
         [
          { $match: { taskId: 164 } },
          { $project: {"deviceId": 1, "mem": 1, "cpu": 1, "fps": 1, "wifiDown": 1, "wifiUp": 1} },
          { $group: { _id: "$deviceId", avg_mem: {$avg: "$mem"}, avg_cpu: {$avg: "$cpu"}, avg_fps: {$avg: "$fps"}, avg_wifiDown: {$avg: "$wifiDown"}}}
        ]
       )
     *@Param: [taskId]
     *@Return: java.util.List<java.util.Map>
     *@Author: wen
     *@Date: 2018/5/23
     */
    public List<Map> getPerformanceStatis(Long taskId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskId").is(taskId)),
                Aggregation.project("deviceId","mem","cpu","fps","wifiDown","wifiUp")
                        .andExpression("mem/1024").as("ram")
                        .andExpression("wifiDown").as("down")
                        .andExpression("wifiUp").as("up"),
                Aggregation.group("deviceId")
                        .avg("ram").as("avg_mem")
                        .avg("cpu").as("avg_cpu")
                        .avg("fps").as("avg_fps")
                        .sum("down").as("sum_wifiDown")
                        .sum("up").as("sum_wifiUp")
        );
        return getResult(agg, Performance.getCollectionName());
    }

    public List<Map> getPerformanceDeviceStatis(Long taskId, String deviceId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(Criteria.where("taskId").is(taskId).and("deviceId").is(deviceId))),
                Aggregation.project("deviceId","mem","cpu","fps","wifiDown","wifiUp")
                        .andExpression("mem/1024").as("ram")
                        .andExpression("wifiDown").as("down")
                        .andExpression("wifiUp").as("up"),
                Aggregation.group("deviceId")
                        .avg("ram").as("avg_mem")
                        .avg("cpu").as("avg_cpu")
                        .avg("fps").as("avg_fps")
                        .sum("down").as("sum_wifiDown")
                        .sum("up").as("sum_wifiUp")
        );
        return getResult(agg, Performance.getCollectionName());
    }

    /**
     *@Description:  步骤时间统计
     * ﻿db.t_step.aggregate(
     *   [
     *    { $match: { taskId: 164, methodDesc: 'installApp'} },
     *    { $project: {"runtime": 1, "methodDesc": 1, "deviceId": 1} },
     *    { $group: { _id: "$deviceId", avg_time: {$avg: "$runtime"}}}
     *    { $sort: {avg_time: 1} }
     *  ]
     * )
     *@Param: [taskId]
     *@Return: java.util.List<java.util.Map>  {'deviceId': xxxxx, 'avg_time': xxxx}
     *@Author: wen
     *@Date: 2018/5/23
     */
    public List<Map> getStepRuntimeStatis(Long taskId, String actionName) {
        List<String> actions = Arrays.asList(StepRequest.StepAction.installApp.name(), StepRequest.StepAction.launch.name(), StepRequest.StepAction.uninstallApp.name());
        if(!actions.contains(actionName)){
            return new ArrayList<>();
        }
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(Criteria.where("taskId").is(taskId).and("action").is(actionName))),
                Aggregation.project("runtime","methodDesc","deviceId")
                        .andExpression("runtime/1000").as("rt"),
                Aggregation.group("deviceId")
                        .avg("rt").as("avg_time"),
                Aggregation.sort(Sort.Direction.ASC, "avg_time")
        );
        return getResult(agg, Step.getCollectionName());
    }

    public List<Map> getPerformanceMemAvg(Long taskId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskId").is(taskId)),
                Aggregation.project("deviceId","mem")
                        .andExpression("mem/1024").as("ram"),
                Aggregation.group("deviceId")
                        .avg("ram").as("avg_value"),
                Aggregation.sort(Sort.Direction.ASC, "avg_value")
        );
        return getResult(agg, Performance.getCollectionName());
    }

    public List<Map> getPerformanceCPUAvg(Long taskId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskId").is(taskId)),
                Aggregation.project("deviceId","cpu"),
                Aggregation.group("deviceId")
                        .avg("cpu").as("avg_value"),
                Aggregation.sort(Sort.Direction.ASC, "avg_value")
        );
        return getResult(agg, Performance.getCollectionName());
    }

}
