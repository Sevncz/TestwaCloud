package com.testwa.distest.server.mongo.event;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.entity.Script;
import com.testwa.distest.server.mongo.model.ProcedureInfo;
import com.testwa.distest.server.mongo.model.ProcedureStatis;
import com.testwa.distest.server.mongo.service.ProcedureInfoService;
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
    private ProcedureInfoService procedureInfoService;
    @Autowired
    private TaskService taskService;

    @Async
    @Override
    public void onApplicationEvent(TaskOverEvent e) {
        log.info("start...");
        Long taskId = e.getTaskId();
        // 根据前端需求开始统计报告
        Task task = taskService.findOne(taskId);
        // 脚本数量
        List<Script> taskScripts = task.getScriptList();
        int scriptNum = taskScripts.size();

        // 统计cpu平均占用率
        List<Map> cpus = statisCpuRate(taskId);

        // 统计内存平均占用量
        List<Map> mems = statisMemory(taskId);

        // 成功和失败步骤数量
        List<Map> statusProcedure = statisStatus(taskId);

        // 成功和失败session数量
        List<Map> sessions = statisScript(taskId);
        List<Map> statusScripts = new ArrayList<>();
        Map<String, Integer> d = new HashMap<>();
        for(Map s : sessions){

            List<ProcedureInfo> l = procedureInfoService.findBySessionId((String) s.get("_id"));
            if(l != null && l.size() > 0){
                ProcedureInfo pi = l.get(0);
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


        ProcedureStatis old = procedureInfoService.getProcedureStatisByExeId(taskId);
        if(old != null){
            procedureInfoService.deleteStatisById(old.getId());
        }
        ProcedureStatis ps = new ProcedureStatis();
        ps.setExeId(taskId);
        ps.setCpurateInfo(cpus);
        ps.setMemoryInfo(mems);
        ps.setStatusProcedureInfo(statusProcedure);
        ps.setStatusScriptInfo(statusScripts);
        ps.setScriptNum(scriptNum);

        procedureInfoService.saveProcedureStatis(ps);
    }

    private List<Map> statisScript(Long exeId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(exeId)),
                Aggregation.group("sessionId").sum("status").as("count")
        );
        return getResult(agg);
    }

    private List<Map> statisStatus(Long exeId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(exeId)),
                Aggregation.group("deviceId", "status").count().as("count")
        );
        return getResult(agg);
    }

    private List<Map> statisMemory(Long exeId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(exeId)),
                Aggregation.group("deviceId").avg("memory").as("value")
        );
        return getResult(agg);
    }

    private List<Map> statisCpuRate(Long exeId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(exeId)),
                Aggregation.group("deviceId").avg("cpurate").as("value")
        );
        return getResult(agg);
    }

    private List<Map> getResult(Aggregation agg) {
        AggregationResults<BasicDBObject> outputType = mongoTemplate.aggregate(agg, "t_procedure_info", BasicDBObject.class);
        List<Map> result = new ArrayList<>();
        for (Iterator<BasicDBObject> iterator = outputType.iterator(); iterator.hasNext();) {
            DBObject obj =iterator.next();
            result.add(obj.toMap());
        }
        return result;
    }

}
