package com.testwa.distest.server.service.task.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mvc.model.ProcedureInfo;
import com.testwa.distest.server.mvc.model.ProcedureStatis;
import com.testwa.distest.server.mvc.repository.ProcedureInfoRepository;
import com.testwa.distest.server.mvc.repository.ProcedureStatisRepository;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.task.dao.ITaskDAO;
import com.testwa.distest.server.service.task.form.TaskListForm;
import com.testwa.distest.server.service.task.form.TaskSceneListForm;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
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
        // app 基本情况
        result.put("appStaty", task.getApp());
        List<Map> statusScript = ps.getStatusScriptInfo();

        Map<String, Device> devInfo = new HashMap<>();
        Map<String, List> devCpuLine = new HashMap<>();
        Map<String, List> devRawLine = new HashMap<>();
        // 设备基本情况
        task.getDevices().forEach( device -> {

            devInfo.put(device.getDeviceId(), device);
            devCpuLine.put(device.getDeviceId(), new ArrayList());
            devRawLine.put(device.getDeviceId(), new ArrayList());
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
            subInfo.put("dto", d.getModel());
            subInfo.put("brand", d.getBrand());
            scriptStaty.add(subInfo);
        });

        result.put("scriptStaty", scriptStaty);

        // cpu 平均消耗
        /*
        {
          "name": "Xiao Mi",
          "value": 89.4
        }
         */
        List<Map> cpuAvgRate = ps.getCpurateInfo();
        List<Map> cpuStaty = new ArrayList<>();
        cpuAvgRate.forEach( s -> {
            String deviceId = (String) s.get("_id");
            DeviceAndroid d = (DeviceAndroid) devInfo.get(deviceId);
            Map<String, Object> subInfo = new HashMap<>();
            subInfo.put("dto", d.getModel());
            subInfo.put("brand", d.getBrand());
            subInfo.put("value", s.get("value"));
            cpuStaty.add(subInfo);
        });
        result.put("cpuStaty", cpuStaty);

        // 内存 平均消耗
        /*
        {
          "name": "Xiao Mi",
          "value": 89.4
        }
         */
        List<Map> memAvgRate = ps.getMemoryInfo();
        List<Map> ramStaty = new ArrayList<>();
        memAvgRate.forEach( s -> {
            String deviceId = (String) s.get("_id");
            DeviceAndroid d = (DeviceAndroid) devInfo.get(deviceId);
            Map<String, Object> subInfo = new HashMap<>();
            subInfo.put("dto", d.getModel());
            subInfo.put("brand", d.getBrand());
            subInfo.put("value", s.get("value"));
            ramStaty.add(subInfo);
        });
        result.put("ramStaty", ramStaty);

        // 内存和cpu时刻消耗
        /*
        {
          "name": "Xiao Mi",
          "series": [
            {
              "value": 69,
              "name": "2016-09-18T05:24:05.254Z"
            },
            {
              "value": 45,
              "name": "2016-09-18T10:21:55.123Z"
            },
            {
              "value": 39,
              "name": "2016-09-18T17:55:43.226Z"
            },
            {
              "value": 54,
              "name": "2016-09-18T20:13:42.627Z"
            },
            {
              "value": 49,
              "name": "2016-09-18T22:28:50.058Z"
            }
          ]
        }
         */
        List<ProcedureInfo> detailInfo = procedureInfoRepository.findByExecutionTaskIdOrderByTimestampAsc(task.getId());

        List<Map> cpuline = new ArrayList<>();
        List<Map> rawline = new ArrayList<>();
        detailInfo.forEach( d -> {
            Map<String, Object> cpuPoint = new HashMap<>();
            cpuPoint.put("value", d.getCpurate());
            cpuPoint.put("name", TimeUtil.formatTimeStamp(d.getTimestamp()));
            devCpuLine.get(d.getDeviceId()).add(cpuPoint);
            Map<String, Object> rawPoint = new HashMap<>();
            rawPoint.put("value", d.getMemory());
            rawPoint.put("name", TimeUtil.formatTimeStamp(d.getTimestamp()));
            devRawLine.get(d.getDeviceId()).add(rawPoint);
        });

        devCpuLine.forEach( (d, l) -> {

            DeviceAndroid tDevice = (DeviceAndroid) devInfo.get(d);
            Map<String, Object> subInfo = new HashMap<>();
            subInfo.put("dto", tDevice.getModel());
            subInfo.put("brand", tDevice.getBrand());
            subInfo.put("series", l);
            cpuline.add(subInfo);
        });

        devRawLine.forEach( (d, l) -> {

            DeviceAndroid tDevice = (DeviceAndroid) devInfo.get(d);
            Map<String, Object> subInfo = new HashMap<>();
            subInfo.put("dto", tDevice.getModel());
            subInfo.put("brand", tDevice.getBrand());
            subInfo.put("series", l);
            rawline.add(subInfo);
        });


        result.put("cpuLine", cpuline);
        result.put("rawLine", rawline);

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

}
