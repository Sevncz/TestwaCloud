package com.testwa.distest.server.service.task.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.testwa.core.base.vo.PageResult;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.*;
import com.testwa.distest.server.mongo.model.ExecutorLogInfo;
import com.testwa.distest.server.mongo.model.ProcedureInfo;
import com.testwa.distest.server.mongo.model.ProcedureStatis;
import com.testwa.distest.server.mongo.repository.ExecutorLogInfoRepository;
import com.testwa.distest.server.mongo.repository.ProcedureInfoRepository;
import com.testwa.distest.server.mongo.repository.ProcedureStatisRepository;
import com.testwa.distest.server.service.project.service.ProjectService;
import com.testwa.distest.server.service.task.dao.ITaskDAO;
import com.testwa.distest.server.service.task.dto.TaskDeviceStatusStatis;
import com.testwa.distest.server.service.task.form.ScriptListForm;
import com.testwa.distest.server.service.task.form.TaskListForm;
import com.testwa.distest.server.web.task.vo.TaskProgressLineVO;
import com.testwa.distest.server.web.task.vo.TaskProgressVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
    @Autowired
    private ExecutorLogInfoRepository executorLogInfoRepository;
    @Autowired
    private TaskDeviceService taskDeviceService;


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

        if(ps != null){

            Map<String, Device> devInfo = new HashMap<>();
            // 设备基本情况
            task.getDevices().forEach( device -> {
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
            statusScript.forEach( s -> {
                String deviceId = (String) s.get("deviceId");
                Map<String, Object> subInfo = new HashMap<>();
                subInfo.put("deviceId", deviceId);
                subInfo.put("successNum", s.get("success"));
                subInfo.put("failedNum", s.get("fail"));
                subInfo.put("total", ps.getScriptNum());
                Device d = devInfo.get(deviceId);
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
                Device d = (Device) devInfo.get(deviceId);
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
                Device d = (Device) devInfo.get(deviceId);
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
            // 数据库存放的是UTC时间
            DateTime dt = new DateTime(task.getCreateTime(), DateTimeZone.UTC);
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
        List<TaskProgressLineVO> lines = new ArrayList<>();
        dateMap.forEach( (k, v) -> {
            TaskProgressLineVO line = new TaskProgressLineVO();
            line.setName(deviceMap.get(k));
            line.setData(v);
            lines.add(line);
        });
        TaskProgressVO vo = new TaskProgressVO();
        vo.setLineList(lines);
        vo.setDeviceNameList(new ArrayList<>(deviceMap.values()));
        return vo;
    }
}
