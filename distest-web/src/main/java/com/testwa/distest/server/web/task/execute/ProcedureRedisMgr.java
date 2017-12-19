package com.testwa.distest.server.web.task.execute;

import com.testwa.core.redis.RedisCacheManager;
import com.testwa.distest.server.mvc.model.ProcedureInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProcedureRedisMgr {

    private static final String procedure_key = "procedureInfo";
    @Autowired
    private RedisCacheManager redisCacheMgr;

    public void addProcedureToQueue(String procedureInfo){
        redisCacheMgr.lpush(procedure_key, procedureInfo);
    }

    public String getProcedureFromQueue(){
        return (String) redisCacheMgr.rpop(procedure_key, String.class);
    }

    public Long size() {
        return redisCacheMgr.llen(procedure_key);
    }
}
