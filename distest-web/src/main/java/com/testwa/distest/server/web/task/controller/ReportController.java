package com.testwa.distest.server.web.task.controller;

import com.testwa.core.entity.Task;
import com.testwa.distest.common.constant.Result;
import com.testwa.distest.common.constant.WebConstants;
import com.testwa.distest.common.exception.ObjectNotExistsException;
import com.testwa.distest.server.mvc.model.ProcedureStatis;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.web.task.validator.ExecutionTaskValidatoer;
import com.testwa.distest.server.web.task.validator.TaskValidatoer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 24/10/2017.
 */
@Api("任务报告相关api")
@RestController
@RequestMapping(path = WebConstants.API_PREFIX + "/report")
public class ReportController {

    @Autowired
    private TaskService executionTaskService;
    @Autowired
    private TaskValidatoer taskValidatoer;
    @Autowired
    private ExecutionTaskValidatoer executionTaskValidatoer;

    @ApiOperation(value="任务执行统计")
    @ResponseBody
    @RequestMapping(value = "/execut/statis", method = RequestMethod.GET)
    public Result statis(@RequestParam(value = "exeId", required = true) Long exeId) throws ObjectNotExistsException {

        // app 基本情况
        Task et = executionTaskValidatoer.validateExecutionTaskExist(exeId);
        Map<String, Object> result = new HashMap<>();
        result.put("appStaty", et.getApp());


        ProcedureStatis ps = executionTaskService.executionTaskStatis(exeId);
        List<Map> statusScript = ps.getStatusScriptInfo();

        Map<String, TDevice> devInfo = new HashMap<>();
        Map<String, List> devCpuLine = new HashMap<>();
        Map<String, List> devRawLine = new HashMap<>();
        // 设备基本情况
        et.getDevices().forEach( device -> {

            devInfo.put(device.getId(), device);
            devCpuLine.put(device.getId(), new ArrayList());
            devRawLine.put(device.getId(), new ArrayList());
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
            TDevice d = devInfo.get(deviceId);
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
            TDevice d = devInfo.get(deviceId);
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
            TDevice d = devInfo.get(deviceId);
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
        List<ProcedureInfo> detailInfo = procedureInfoService.findByExeId(exeId);

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

            TDevice tDevice = devInfo.get(d);
            Map<String, Object> subInfo = new HashMap<>();
            subInfo.put("dto", tDevice.getModel());
            subInfo.put("brand", tDevice.getBrand());
            subInfo.put("series", l);
            cpuline.add(subInfo);
        });

        devRawLine.forEach( (d, l) -> {

            TDevice tDevice = devInfo.get(d);
            Map<String, Object> subInfo = new HashMap<>();
            subInfo.put("dto", tDevice.getModel());
            subInfo.put("brand", tDevice.getBrand());
            subInfo.put("series", l);
            rawline.add(subInfo);
        });


        result.put("cpuLine", cpuline);
        result.put("rawLine", rawline);


        return ok(result);
    }

}
