package com.testwa.distest.server.service.cache.mgr;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 任务数量管理，计数器实现，增加一个subtask，加1，完成一个subtask，减1
 */
@Slf4j
@Component
public class TaskCountMgr {
    private static final String KEY_PREFIX = "count.task.";

    @Resource
    private RedissonClient redissonClient;


    public void incrSubTaskCount(Long taskCode) {
        String key = KEY_PREFIX + taskCode;
        RAtomicLong bucket = redissonClient.getAtomicLong(key);
        if(!bucket.isExists()) {
            bucket.set(1L);
        }else{
            bucket.incrementAndGet();
        }
    }

    public void decrSubTaskCount(Long taskCode) {
        String key = KEY_PREFIX + taskCode;
        RAtomicLong bucket = redissonClient.getAtomicLong(key);
        if(bucket.isExists()) {
            bucket.decrementAndGet();
        }
    }

    public Long getSubTaskCount(Long taskCode) {
        String key = KEY_PREFIX + taskCode;
        RAtomicLong bucket = redissonClient.getAtomicLong(key);
        Long result = bucket.get();
        return result;
    }

}
