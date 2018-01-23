package com.testwa.distest.server.web.task.execute;

import com.testwa.core.redis.RedisCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcedureRedisMgr {

    private static final String procedure_key = "procedureInfo";
    private static final String procedure_error_key = "procedureInfo.error";
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

    public void addErrorProcedureToQueue(String info) {
        redisCacheMgr.lpush(procedure_error_key, info);
    }
}
