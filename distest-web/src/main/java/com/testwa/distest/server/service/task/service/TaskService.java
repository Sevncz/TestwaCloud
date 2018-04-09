package com.testwa.distest.server.service.task.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mongo.model.ProcedureInfo;
import com.testwa.distest.server.mongo.model.ProcedureStatis;
import com.testwa.distest.server.mongo.repository.ProcedureInfoRepository;
import com.testwa.distest.server.mongo.repository.ProcedureStatisRepository;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.task.dao.ITaskDAO;
import com.testwa.distest.server.service.task.form.ScriptListForm;
import com.testwa.distest.server.service.task.form.TaskListForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.testwa.distest.common.util.WebUtil.getCurrentUsername;

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
            List<ProcedureInfo> infos = procedureInfoRepository.findByExecutionTaskIdOrderByTimestampAsc(id);
            procedureInfoRepository.delete(infos);
        });
    }


    public List<Task> getRunningTask(Long projectId, Long userId) {
        Task query = new Task();
        query.setStatus(DB.TaskStatus.RUNNING);
        query.setProjectId(projectId);
        query.setCreateBy(userId);
        return taskDAO.findBy(query);
    }

    public List<Task> getRecentFinishedRunningTask(Long projectId, Long userId) {
        Task query = new Task();
        query.setStatus(DB.TaskStatus.COMPLETE);
        query.setProjectId(projectId);
        query.setCreateBy(userId);
        return taskDAO.findBy(query);
    }

    public Map<String, Object> statis(Task task) {
        ProcedureStatis ps = procedureStatisRepository.findByExeId(task.getId());
        Map<String, Object> result = new HashMap<>();
        if(ps != null){

            // app 基本情况
            result.put("appStaty", task.getApp());
            List<Map> statusScript = ps.getStatusScriptInfo();

            Map<String, Device> devInfo = new HashMap<>();
            // 设备基本情况
            task.getDevices().forEach( device -> {
                devInfo.put(device.getDeviceId(), device);
            });

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
            statusScript.forEach( s -> {
                String deviceId = (String) s.get("deviceId");
                Map<String, Object> subInfo = new HashMap<>();
                subInfo.put("deviceId", deviceId);
                subInfo.put("successNum", s.get("success"));
                subInfo.put("failedNum", s.get("fail"));
                subInfo.put("total", ps.getScriptNum());
                DeviceAndroid d = (DeviceAndroid) devInfo.get(deviceId);
                if(d != null){
                    subInfo.put("dto", d.getModel());
                    subInfo.put("brand", d.getBrand());
                    scriptStaty.add(subInfo);
                }
            });
            result.put("scriptStaty", scriptStaty);

            // cpu 平均消耗
            /*
                cpuStaty = {
                    name: ['nubia', 'xiaomi'],
                    value: [6.2, 3.3]
                }
             */
            List<Map> cpuAvgRate = ps.getCpurateInfo();
            List cpuName = new ArrayList();
            List cpuValue = new ArrayList();
            cpuAvgRate.forEach( s -> {
                String deviceId = (String) s.get("_id");
                DeviceAndroid d = (DeviceAndroid) devInfo.get(deviceId);
                if(d != null) {
                    cpuName.add(d.getBrand());
                    cpuValue.add(s.get("value"));
                }
            });
            Map<String, List> cpuStaty = new HashMap();
            cpuStaty.put("name", cpuName);
            cpuStaty.put("value", cpuValue);
            result.put("cpuStaty", cpuStaty);

            // 内存 平均消耗
            /*
                ramStaty = {
                    name: ['nubia', 'xiaomi'],
                    value: [6.2, 3.3]
                }
             */
            List<Map> memAvgRate = ps.getMemoryInfo();
            List rawName = new ArrayList();
            List rawValue = new ArrayList();
            memAvgRate.forEach( s -> {
                String deviceId = (String) s.get("_id");
                DeviceAndroid d = (DeviceAndroid) devInfo.get(deviceId);
                Map<String, Object> subInfo = new HashMap<>();
                if(d != null) {
                    rawName.add(d.getBrand());
                    rawValue.add(s.get("value"));
                }
            });
            Map<String, List> ramStaty = new HashMap();
            ramStaty.put("name", rawName);
            ramStaty.put("value", rawValue);
            result.put("ramStaty", ramStaty);

            // 内存和cpu时刻消耗
            /*
            cpuLine = {
                    name: ['nubia', 'xiaomo'],
                    value: [
                        [
                            ["2018-03-27 22:17:04", 18],
                            ["2018-03-27 22:17:04", 18],
                            ["2018-03-27 22:17:04", 18]
                        ],
                        [
                            ["2018-03-27 22:17:04", 18],
                            ["2018-03-27 22:17:04", 18],
                            ["2018-03-27 22:17:04", 18]
                        ],
                    ]
                }
             */

            Map<String, List> cpuline = new HashMap<>();
            Map<String, List> rawline = new HashMap<>();

            List devNameList = new ArrayList();
            List cpuLineValue = new ArrayList();
            List rawLineValue = new ArrayList();

            devInfo.forEach( (k,v) -> {
                List<ProcedureInfo> devDetailInfo = procedureInfoRepository.findByExecutionTaskIdAndDeviceIdOrderByTimestampAsc(task.getId(), k);
                devNameList.add(v.getBrand());
                List cpuDevLineValue = new ArrayList();
                List rawDevLineValue = new ArrayList();
                devDetailInfo.forEach( data -> {
                    List cpuPoint = new ArrayList();
                    cpuPoint.add(TimeUtil.formatTimeStamp(data.getTimestamp()));
                    cpuPoint.add(data.getCpurate());
                    cpuDevLineValue.add(cpuPoint);

                    List rawPoint = new ArrayList();
                    rawPoint.add(TimeUtil.formatTimeStamp(data.getTimestamp()));
                    rawPoint.add(data.getMemory());
                    rawDevLineValue.add(rawPoint);
                });
                cpuLineValue.add(cpuDevLineValue);
                rawLineValue.add(rawDevLineValue);
            });

            cpuline.put("name", devNameList);
            cpuline.put("value", cpuLineValue);

            rawline.put("name", devNameList);
            rawline.put("value", rawLineValue);

            result.put("cpuLine", cpuline);
            result.put("ramLine", rawline);
            result.put("devInfo", task.getDevices());
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
        PageInfo<Task> info = new PageInfo(entityList);
        PageResult<Task> pr = new PageResult<>(info.getList(), info.getTotal());
        return pr;
    }

    public List<Script> findScriptListInTask(ScriptListForm form) {
        Task task = taskDAO.findOne(form.getTaskId());
        return task.getScriptList();
    }
}
