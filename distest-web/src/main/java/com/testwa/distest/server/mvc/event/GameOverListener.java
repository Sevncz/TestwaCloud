package com.testwa.distest.server.mvc.event;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.testwa.distest.server.mvc.model.ProcedureStatis;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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

    @Async
    @Override
    public void onApplicationEvent(GameOverEvent e) {
        log.info("start...");
        String exeId = e.getExeId();
        // 根据前端需求开始统计报告

        // 统计cpu平均占用率
        List<Map> cpus = statisCpuRate(exeId);

        // 统计内存平均占用量
        List<Map> mems = statisMemory(exeId);

        // 成功和失败数量
        List<Map> status = statisStatus(exeId);


        ProcedureStatis s = procedureInfoService.getProcedureStatisByExeId(exeId);
        if(s == null){
            s = new ProcedureStatis();
            s.setExeId(exeId);
        }
        s.setCpurateInfo(cpus);
        s.setMemoryInfo(mems);
        s.setStatusInfo(status);

        procedureInfoService.saveProcedureStatis(s);
    }

    private List<Map> statisStatus(String exeId) {
        Aggregation status = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(exeId)),
                Aggregation.group("deviceId", "status").count().as("count")
        );
        AggregationResults<BasicDBObject> outputType = mongoTemplate.aggregate(status, "t_procedure_info", BasicDBObject.class);
        List<Map> result = new ArrayList<>();
        for (Iterator<BasicDBObject> iterator = outputType.iterator(); iterator.hasNext();) {
            DBObject obj =iterator.next();
            result.add(obj.toMap());
        }
        return result;
    }

    private List<Map> statisMemory(String exeId) {
        Aggregation mem = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(exeId)),
                Aggregation.group("deviceId").avg("memory").as("value")
        );
        AggregationResults<BasicDBObject> outputType = mongoTemplate.aggregate(mem, "t_procedure_info", BasicDBObject.class);
        List<Map> result = new ArrayList<>();
        for (Iterator<BasicDBObject> iterator = outputType.iterator(); iterator.hasNext();) {
            DBObject obj =iterator.next();
            result.add(obj.toMap());
        }
        return result;
    }

    private List<Map> statisCpuRate(String exeId) {
        Aggregation cpu = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("executionTaskId").is(exeId)),
                Aggregation.group("deviceId").avg("cpurate").as("value")
        );
        AggregationResults<BasicDBObject> outputType = mongoTemplate.aggregate(cpu, "t_procedure_info", BasicDBObject.class);
        List<Map> result = new ArrayList<>();
        for (Iterator<BasicDBObject> iterator = outputType.iterator(); iterator.hasNext();) {
            DBObject obj =iterator.next();
            result.add(obj.toMap());
        }
        return result;
    }

}
