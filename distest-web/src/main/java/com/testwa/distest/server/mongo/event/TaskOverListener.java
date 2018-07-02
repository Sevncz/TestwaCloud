package com.testwa.distest.server.mongo.event;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.mongo.model.AppiumRunningLog;
import com.testwa.distest.server.mongo.model.ProcedureStatis;
import com.testwa.distest.server.mongo.service.AppiumRunningLogService;
import com.testwa.distest.server.service.device.service.DeviceService;
import com.testwa.distest.server.service.task.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * Created by wen on 19/08/2017.
 */
@Slf4j
@Component
public class TaskOverListener implements ApplicationListener<TaskOverEvent> {
    @Autowired
    private MongoOperations mongoTemplate;
    @Autowired
    private AppiumRunningLogService procedureInfoService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private DeviceService deviceService;

    @Async
    @Override
    public void onApplicationEvent(TaskOverEvent e) {
        log.info("start...");
        Long taskCode = e.getTaskCode();
        log.info("........ update task info");
        if(e.isTimeout()) {
            taskService.timeout(taskCode);
        }else{
            taskService.complete(taskCode);
        }
        // 根据前端需求开始统计报告
        Task task = taskService.findByCode(taskCode);

        task.getDevices().forEach(d -> {
            deviceService.release(d.getDeviceId());
        });

        if(DB.TaskType.HG.equals(task.getTaskType())){
            hgtaskStatis(task);
        }
        if(DB.TaskType.JR.equals(task.getTaskType())){

        }
    }

    private void hgtaskStatis(Task task) {
        Long taskCode = task.getTaskCode();
        // 脚本数量
        List<Script> taskScripts = task.getScriptList();
        int scriptNum = taskScripts.size();

        // 统计cpu平均占用率
        List<Map> cpus = statisCpuRate(taskCode);

        // 统计内存平均占用量
        List<Map> mems = statisMemory(taskCode);

        // 成功和失败步骤数量
        List<Map> statusProcedure = statisStatus(taskCode);

        // 成功和失败session数量
        List<Map> sessions = statisScript(taskCode);
        List<Map> statusScripts = new ArrayList<>();
        Map<String, Integer> d = new HashMap<>();
        for(Map s : sessions){

            List<AppiumRunningLog> l = procedureInfoService.findBySessionId((String) s.get("_id"));
            if(l != null && l.size() > 0){
                AppiumRunningLog pi = l.get(0);
                Integer scriptFailNum = d.getOrDefault(pi.getDeviceId(), 0);
                if((Integer) s.get("count") > 0){
                    scriptFailNum += 1;
                }
                d.put(pi.getDeviceId(), scriptFailNum);
            }
        }
        int finalScriptNum = scriptNum;
        d.forEach((k, v) -> {
            Map<String, Object> t = new HashMap<>();
            t.put("deviceId", k);
            t.put("fail", v);
            t.put("success", finalScriptNum - v);
            statusScripts.add(t);
        });


        ProcedureStatis old = procedureInfoService.getProcedureStatisByExeId(taskCode);
        if(old != null){
            procedureInfoService.deleteStatisById(old.getId());
        }
        ProcedureStatis ps = new ProcedureStatis();
        ps.setTaskCode(taskCode);
        ps.setCpurateInfo(cpus);
        ps.setMemoryInfo(mems);
        ps.setStatusProcedureInfo(statusProcedure);
        ps.setStatusScriptInfo(statusScripts);
        ps.setScriptNum(scriptNum);

        procedureInfoService.saveProcedureStatis(ps);
    }

    private List<Map> statisScript(Long taskCode) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskCode").is(taskCode)),
                Aggregation.group("sessionId").sum("status").as("count")
        );
        return getResult(agg);
    }

    private List<Map> statisStatus(Long taskCode) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskCode").is(taskCode)),
                Aggregation.group("deviceId", "status").count().as("count")
        );
        return getResult(agg);
    }

    private List<Map> statisMemory(Long taskCode) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskCode").is(taskCode)),
                Aggregation.group("deviceId").avg("memory").as("value")
        );
        return getResult(agg);
    }

    private List<Map> statisCpuRate(Long taskCode) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("taskCode").is(taskCode)),
                Aggregation.group("deviceId").avg("cpurate").as("value")
        );
        return getResult(agg);
    }

    private List<Map> getResult(Aggregation agg) {
        AggregationResults<BasicDBObject> outputType = mongoTemplate.aggregate(agg, AppiumRunningLog.getCollectionName(), BasicDBObject.class);
        List<Map> result = new ArrayList<>();
        for (Iterator<BasicDBObject> iterator = outputType.iterator(); iterator.hasNext();) {
            DBObject obj =iterator.next();
            result.add(obj.toMap());
        }
        return result;
    }

}
