package com.testwa.distest.server.service.cache.mgr;

import com.testwa.core.redis.RedisCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 任务数量管理，计数器实现，增加一个subtask，加1，完成一个subtask，减1
 */
@Slf4j
@Component
public class TaskCountMgr {
    private static final String KEY_PREFIX = "count.task.";

    @Autowired
    private RedisCacheManager redisCacheMgr;


    public void incrSubTaskCount(Long taskCode) {
        String key = KEY_PREFIX + taskCode;
        if(!redisCacheMgr.existsKey(key)) {
            redisCacheMgr.putString(key, 60*60*24, "1");
        }else{
            redisCacheMgr.incr(key);
        }
    }

    public void decrSubTaskCount(Long taskCode) {
        String key = KEY_PREFIX + taskCode;
        if(redisCacheMgr.existsKey(key)) {
            redisCacheMgr.decr(key);
        }
    }

    public Integer getSubTaskCount(Long taskCode) {
        String key = KEY_PREFIX + taskCode;
        String result = redisCacheMgr.getString(key);
        return StringUtils.isNotBlank(result) ? Integer.parseInt(result) : 0;
    }

}
