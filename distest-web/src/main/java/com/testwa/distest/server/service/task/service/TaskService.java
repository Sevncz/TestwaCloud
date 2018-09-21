package com.testwa.distest.server.service.task.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.testwa.core.base.vo.PageResult;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mongo.model.*;
import com.testwa.distest.server.mongo.repository.*;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.task.dao.ITaskDAO;
import com.testwa.distest.server.service.task.dto.CountAppTestStatisDTO;
import com.testwa.distest.server.service.task.dto.CountMemberTestStatisDTO;
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
    private AppiumRunningLogRepository procedureInfoRepository;
    @Autowired
    private StepRepository stepRepository;
    @Autowired
    private SubTaskService subTaskService;
    @Autowired
    private MongoOperations mongoTemplate;


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long save(Task entity) {
        return taskDAO.insert(entity);
    }

    public Task findOne(Long entityId) {
        return taskDAO.findOne(entityId);
    }

    public Task findByCode(Long taskCode) {
        return taskDAO.findByCode(taskCode);
    }
    public List<Task> findAll(List<Long> entityIds) {
        return taskDAO.findAll(entityIds);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void update(Task entity) {
        taskDAO.update(entity);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void disableAll(List<Long> taskCodes) {
        taskDAO.disableAll(taskCodes);
    }

    public Map<String, Object> statis(Task task) {
        Map<String, Object> result = new HashMap<>();
        // taskType
        result.put("taskType", task.getTaskType());
        // taskStatus
        List<TaskDeviceStatusStatis> tds = subTaskService.countTaskDeviceStatus(task.getTaskCode());
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


    public PageResult<Task> findPage(Long projectId, TaskListForm pageForm) {
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        List<Task> entityList = findList(projectId, pageForm);
        PageInfo<Task> info = new PageInfo(entityList);
        PageResult<Task> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public PageResult<Task> findFinishPage(Long projectId, TaskListForm pageForm) {
        //分页处理
        PageHelper.startPage(pageForm.getPageNo(), pageForm.getPageSize());
        List<Task> entityList = findFinishList(projectId, pageForm);
        PageInfo<Task> info = new PageInfo(entityList);
        PageResult<Task> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<Task> findList(Long projectId, TaskListForm queryForm) {
        PageHelper.orderBy(queryForm.getOrderBy() + " " + queryForm.getOrder());
        Task query = new Task();
        query.setProjectId(projectId);
        if (StringUtils.isNotBlank(queryForm.getTaskName())) {
            query.setTaskName(queryForm.getTaskName());
        }
        if (queryForm.getAppId() != null) {
            query.setAppId(queryForm.getAppId());
        }
        List<Task> entityList = taskDAO.findBy(query);

        entityList.forEach(entity -> {
            List<TaskDeviceStatusStatis> tds = subTaskService.countTaskDeviceStatus(entity.getId());
            entity.setDeviceStatusStatis(tds);
        });
        return entityList;
    }

    public List<Task> findFinishList(Long projectId) {
        return findFinishList(projectId, new TaskListForm());
    }


    public List<Task> findFinishList(Long projectId, Long startTime, Long endTime, TaskListForm queryForm) {
        PageHelper.orderBy(queryForm.getOrderBy() + " " + queryForm.getOrder());
        Task query = new Task();
        query.setProjectId(projectId);
        if (StringUtils.isNotBlank(queryForm.getTaskName())) {
            query.setTaskName(queryForm.getTaskName());
        }
        if (queryForm.getAppId() != null) {
            query.setAppId(queryForm.getAppId());
        }
        List<Task> entityList = taskDAO.findFinishBy(query, startTime, endTime);

        entityList.forEach(entity -> {
            List<TaskDeviceStatusStatis> tds = subTaskService.countTaskDeviceStatus(entity.getId());
            entity.setDeviceStatusStatis(tds);
        });
        return entityList;
    }

    public List<Task> findFinishList(Long projectId, TaskListForm queryForm) {
        PageHelper.orderBy(queryForm.getOrderBy() + " " + queryForm.getOrder());
        Task query = new Task();
        query.setProjectId(projectId);
        if (StringUtils.isNotBlank(queryForm.getTaskName())) {
            query.setTaskName(queryForm.getTaskName());
        }
        if (queryForm.getAppId() != null) {
            query.setAppId(queryForm.getAppId());
        }
        List<Task> entityList = taskDAO.findFinishBy(query);

        entityList.forEach(entity -> {
            List<TaskDeviceStatusStatis> tds = subTaskService.countTaskDeviceStatus(entity.getId());
            entity.setDeviceStatusStatis(tds);
        });
        return entityList;
    }

    public List<Script> findScriptListInTask(ScriptListForm form) {
        Task task = taskDAO.findOne(form.getTaskCode());
        return task.getScriptList();
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void complete(Long taskCode) {
        Date endTime = new Date();
        taskDAO.finish(taskCode, endTime, DB.TaskStatus.COMPLETE);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void timeout(Long taskCode) {
        Date endTime = new Date();
        taskDAO.finish(taskCode, endTime, DB.TaskStatus.TIMEOUT);
    }


    public List<Map> getAppiumRunningLogStatisByTask(Long taskCode) {

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskCode").is(taskCode)),
                Aggregation.group( "status").count().as("count")
        );

        return getResult(agg, AppiumRunningLog.getCollectionName());
    }

    public List<AppiumRunningLog> getAllInstallSuccessProcedure(Long taskCode) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("taskCode").is(taskCode),
                Criteria.where("status").is(0),
                Criteria.where("command.action").is("安装应用"));

        return procedureInfoRepository.find(new Query(criatira));
    }

    public List<AppiumRunningLog> getAllLanucherSuccessProcedure(Long taskCode) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("taskCode").is(taskCode),
                Criteria.where("status").is(0),
                Criteria.where("command.action").is("启动应用"));
        return procedureInfoRepository.find(new Query(criatira));
    }

    public List<AppiumRunningLog> getAllUninstallSuccessProcedure(Long taskCode) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("taskCode").is(taskCode),
                Criteria.where("status").is(0),
                Criteria.where("command.action").is("卸载应用"));
        return procedureInfoRepository.find(new Query(criatira));
    }

    public List<Step> getSuccessStep(Long taskCode, StepRequest.StepAction uninstallApp) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("taskCode").is(taskCode),
                Criteria.where("status").is(StepRequest.StepStatus.SUCCESS.getNumber()),
                Criteria.where("action").is(uninstallApp.name()));

        return stepRepository.find(new Query(criatira));
    }

    public List<Map> getStepStatusStatis(Long taskCode) {

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskCode").is(taskCode)),
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
            String name;
            if(StringUtils.isBlank(d.getModel())) {
                name = d.getBrand();
            }else{
                name = d.getModel().contains(d.getBrand()) ? d.getModel() : d.getBrand() + " " + d.getModel();
            }
            deviceMap.put(d.getDeviceId(), name);
        });
        return deviceMap;
    }

    public List<Map> getStartUpTime(Long taskCode) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("taskCode").is(taskCode),
                Criteria.where("command.action").is("启动应用"));
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criatira),
                Aggregation.project("runtime", "deviceId").andExpression("runtime/1000").as("second"),
                Aggregation.group("deviceId").avg("second").as("value")
        );
        return getResult(agg, AppiumRunningLog.getCollectionName());
    }

    public List<Map> getInstallTime(Long taskCode) {
        Criteria criatira = new Criteria();
        criatira.andOperator(Criteria.where("taskCode").is(taskCode),
                Criteria.where("command.action").is("安装应用"));
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criatira),
                Aggregation.project("runtime", "deviceId").andExpression("runtime/1000").as("second"),
                Aggregation.group("deviceId").avg("second").as("value")
        );
        return getResult(agg, AppiumRunningLog.getCollectionName());
    }

    public List<Map> getMemoryAvg(Long taskCode) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskCode").is(taskCode)),
                Aggregation.project("memory", "deviceId").andExpression("memory/1024").as("m"),
                Aggregation.group("deviceId").avg("m").as("value")
        );
        return getResult(agg, AppiumRunningLog.getCollectionName());
    }

    public List<Map> getCpuAvg(Long taskCode) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskCode").is(taskCode)),
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

    public List<Map> getSomeActionRuntime(Long taskCode, String deviceId) {
        List<String> actions = Arrays.asList(StepRequest.StepAction.installApp.name(), StepRequest.StepAction.launch.name(), StepRequest.StepAction.uninstallApp.name());

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(Criteria.where("taskCode").is(taskCode).and("action").in(actions).and("deviceId").is(deviceId))),
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
          { $match: { taskCode: 164 } },
          { $project: {"deviceId": 1, "mem": 1, "cpu": 1, "fps": 1, "wifiDown": 1, "wifiUp": 1} },
          { $group: { _id: "$deviceId", avg_mem: {$avg: "$mem"}, avg_cpu: {$avg: "$cpu"}, avg_fps: {$avg: "$fps"}, avg_wifiDown: {$avg: "$wifiDown"}}}
        ]
       )
     *@Param: [taskCode]
     *@Return: java.util.List<java.util.Map>
     *@Author: wen
     *@Date: 2018/5/23
     */
    public List<Map> getPerformanceStatis(Long taskCode) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskCode").is(taskCode)),
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

    public List<Map> getPerformanceDeviceStatis(Long taskCode, String deviceId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(Criteria.where("taskCode").is(taskCode).and("deviceId").is(deviceId))),
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
     *    { $match: { taskCode: 164, methodDesc: 'installApp'} },
     *    { $project: {"runtime": 1, "methodDesc": 1, "deviceId": 1} },
     *    { $group: { _id: "$deviceId", avg_time: {$avg: "$runtime"}}}
     *    { $sort: {avg_time: 1} }
     *  ]
     * )
     *@Param: [taskCode]
     *@Return: java.util.List<java.util.Map>  {'deviceId': xxxxx, 'avg_time': xxxx}
     *@Author: wen
     *@Date: 2018/5/23
     */
    public List<Map> getStepRuntimeStatis(Long taskCode, String actionName) {
        List<String> actions = Arrays.asList(StepRequest.StepAction.installApp.name(), StepRequest.StepAction.launch.name(), StepRequest.StepAction.uninstallApp.name());
        if(!actions.contains(actionName)){
            return new ArrayList<>();
        }
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(Criteria.where("taskCode").is(taskCode).and("action").is(actionName))),
                Aggregation.project("runtime","methodDesc","deviceId")
                        .andExpression("runtime/1000").as("rt"),
                Aggregation.group("deviceId")
                        .avg("rt").as("avg_time"),
                Aggregation.sort(Sort.Direction.ASC, "avg_time")
        );
        return getResult(agg, Step.getCollectionName());
    }

    public List<Map> getPerformanceMemAvg(Long taskCode) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskCode").is(taskCode)),
                Aggregation.project("deviceId","mem")
                        .andExpression("mem/1024").as("ram"),
                Aggregation.group("deviceId")
                        .avg("ram").as("avg_value"),
                Aggregation.sort(Sort.Direction.ASC, "avg_value")
        );
        return getResult(agg, Performance.getCollectionName());
    }

    public List<Map> getPerformanceCPUAvg(Long taskCode) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskCode").is(taskCode)),
                Aggregation.project("deviceId","cpu"),
                Aggregation.group("deviceId")
                        .avg("cpu").as("avg_value"),
                Aggregation.sort(Sort.Direction.ASC, "avg_value")
        );
        return getResult(agg, Performance.getCollectionName());
    }

    public List<CountAppTestStatisDTO> countAppTest(Long projectId, Long startTime, Long endTime) {
        return taskDAO.countAppTest(projectId, startTime, endTime);
    }

    public List<CountMemberTestStatisDTO> countMemberTest(Long projectId, Long startTime, Long endTime) {
        return taskDAO.countMemberTest(projectId, startTime, endTime);
    }
}
