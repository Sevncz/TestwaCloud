package com.testwa.distest.server.web.task.mgr;

import com.alibaba.fastjson.JSON;
import com.testwa.core.redis.RedisCacheManager;
import com.testwa.distest.server.mongo.model.Performance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PerformanceRedisMgr {

    private static final String key = "performance";
    @Autowired
    private RedisCacheManager redisCacheMgr;

    public void addPerformanceToQueue(Performance performance){
        redisCacheMgr.lpush(key, JSON.toJSONString(performance));
    }

    public String getPerformanceFormQueue(){
        return (String) redisCacheMgr.rpop(key, String.class);
    }

    public Long size() {
        return redisCacheMgr.llen(key);
    }

}
