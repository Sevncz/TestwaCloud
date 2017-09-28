package com.testwa.distest.server.mvc.event;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.testwa.distest.server.mvc.model.ExecutionTask;
import com.testwa.distest.server.mvc.model.ProcedureInfo;
import com.testwa.distest.server.mvc.model.ProcedureStatis;
import com.testwa.distest.server.mvc.model.Script;
import com.testwa.distest.server.mvc.service.ExeTaskService;
import com.testwa.distest.server.mvc.service.ProcedureInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Component
public class GameOverListener implements ApplicationListener<GameOverEvent> {
    private static Logger log = LoggerFactory.getLogger(GameOverListener.class);

    @Autowired
    private MongoOperations mongoTemplate;
    @Autowired
    private ProcedureInfoService procedureInfoService;
    @Autowired
    private ExeTaskService exeTaskService;

    @Async
    @Override
    public void onApplicationEvent(GameOverEvent e) {
        log.info("start...");
        String exeId = e.getExeId();
        // 根据前端需求开始统计报告
        ExecutionTask et = exeTaskService.getExeTaskById(exeId);
        // 脚本数量
        Map<String, List<Script>> taskScripts = et.getScripts();
        int scriptNum = 0;
        for(List l : taskScripts.values()){
            scriptNum = scriptNum + l.size();
        }

        // 统计cpu平均占用率
        List<Map> cpus = statisCpuRate(exeId);

        // 统计内存平均占用量
        List<Map> mems = statisMemory(exeId);

        // 成功和失败步骤数量
        List<Map> statusProcedure = statisStatus(exeId);

        // 成功和失败session数量
        List<Map> sessions = statisScript(exeId);
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


        ProcedureStatis old = procedureInfoService.getProcedureStatisByExeId(exeId);
        if(old != null){
            procedureInfoService.deleteStatisById(old.getId());
        }
        ProcedureStatis ps = new ProcedureStatis();
        ps.setExeId(exeId);
        ps.setCpurateInfo(cpus);
        ps.setMemoryInfo(mems);
        ps.setStatusProcedureInfo(statusProcedure);
        ps.setStatusScriptInfo(statusScripts);
        ps.setScriptNum(scriptNum);

        procedureInfoService.saveProcedureStatis(ps);
    }

    private List<Map> statisScript(String exeId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(exeId)),
                Aggregation.group("sessionId").sum("status").as("count")
        );
        return getResult(agg);
    }

    private List<Map> statisStatus(String exeId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(exeId)),
                Aggregation.group("deviceId", "status").count().as("count")
        );
        return getResult(agg);
    }

    private List<Map> statisMemory(String exeId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(exeId)),
                Aggregation.group("deviceId").avg("memory").as("value")
        );
        return getResult(agg);
    }

    private List<Map> statisCpuRate(String exeId) {
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
