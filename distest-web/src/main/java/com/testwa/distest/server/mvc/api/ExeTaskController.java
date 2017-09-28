package com.testwa.distest.server.mvc.api;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.exception.NoSuchExecutionTaskException;
import com.testwa.distest.server.mvc.beans.PageResult;
import com.testwa.distest.server.mvc.beans.Result;
import com.testwa.distest.server.mvc.beans.ResultCode;
import com.testwa.distest.server.mvc.model.*;
import com.testwa.distest.server.mvc.service.ExeTaskService;
import com.testwa.distest.server.mvc.service.ProcedureInfoService;
import com.testwa.distest.server.mvc.service.UserService;
import com.testwa.distest.server.mvc.vo.ExeTaskProgressVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wen on 12/08/2017.
 */
@Api("报告相关api")
@Slf4j
@RestController
@RequestMapping(path = "/api/exeTask")
public class ExeTaskController extends BaseController{

    @Autowired
    private UserService userService;
    @Autowired
    private ExeTaskService exeTaskService;
    @Autowired
    private ProcedureInfoService procedureInfoService;

    @ApiOperation(value="报告分页列表")
    @ResponseBody
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Result page(@RequestParam(value = "page") Integer page,
                       @RequestParam(value = "size") Integer size,
                       @RequestParam(value = "sortField") String sortField,
                       @RequestParam(value = "sortOrder") String sortOrder,
                       @RequestParam String projectId) {

        PageRequest pageRequest = buildPageRequest(page, size, sortField, sortOrder);
        User user = userService.findByUsername(getCurrentUsername());

        Page<ExecutionTask> executionTasks = exeTaskService.findPage(pageRequest, user, projectId);
        PageResult<ExecutionTask> pr = new PageResult<>(executionTasks.getContent(), executionTasks.getTotalElements());
        return ok(pr);
    }

    @ApiOperation(value="查看一个执行任务的进度")
    @ResponseBody
    @RequestMapping(value = "/progress", method = RequestMethod.GET)
    public Result progress(@RequestParam(value = "exeId") String exeId) throws NoSuchExecutionTaskException {
        if(StringUtils.isBlank(exeId)){
            log.error("exeId: {}", exeId);
            return fail(ResultCode.PARAM_ERROR, "参数错误");
        }
        List<ExeTaskProgressVO> result = exeTaskService.getProgress(exeId);
        return ok(result);
    }

    @ApiOperation(value="执行任务统计")
    @ResponseBody
    @RequestMapping(value = "/statis", method = RequestMethod.GET)
    public Result statis(@RequestParam(value = "exeId") String exeId) throws NoSuchExecutionTaskException {
        if(StringUtils.isBlank(exeId)){
            log.error("exeId: {}", exeId);
            return fail(ResultCode.PARAM_ERROR, "参数错误");
        }

        // app 基本情况
        ExecutionTask et = exeTaskService.getExeTaskById(exeId);
        Map<String, Object> result = new HashMap<>();
        result.put("appStaty", et.getApp());


        ProcedureStatis ps = exeTaskService.executionTaskStatis(exeId);
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
         "model": "Task 4",
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
            subInfo.put("model", d.getModel());
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
            subInfo.put("model", d.getModel());
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
            subInfo.put("model", d.getModel());
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
            subInfo.put("model", tDevice.getModel());
            subInfo.put("brand", tDevice.getBrand());
            subInfo.put("series", l);
            cpuline.add(subInfo);
        });

        devRawLine.forEach( (d, l) -> {

            TDevice tDevice = devInfo.get(d);
            Map<String, Object> subInfo = new HashMap<>();
            subInfo.put("model", tDevice.getModel());
            subInfo.put("brand", tDevice.getBrand());
            subInfo.put("series", l);
            rawline.add(subInfo);
        });


        result.put("cpuLine", cpuline);
        result.put("rawLine", rawline);


        return ok(result);
    }

    @ApiOperation(value="获得执行任务用例脚本树")
    @ResponseBody
    @RequestMapping(value = "/taskTree", method = RequestMethod.GET)
    public Result page(@RequestParam String exeId) {
        ExecutionTask executionTask = exeTaskService.getExeTaskById(exeId);
        return ok(executionTask);
    }

    @ApiOperation(value="获得执行任务脚本步骤列表")
    @ResponseBody
    @RequestMapping(value = "/scriptStep", method = RequestMethod.GET)
    public Result page(@RequestParam String deviceId, @RequestParam String exeId, @RequestParam String caseId, @RequestParam String scriptId) {
        List<ProcedureInfo> procedureInfos = exeTaskService.getSteps(deviceId, exeId, caseId, scriptId);
        return ok(procedureInfos);
    }
}
