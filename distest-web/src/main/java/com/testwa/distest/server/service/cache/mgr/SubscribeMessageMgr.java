package com.testwa.distest.server.service.cache.mgr;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class SubscribeMessageMgr {

    /** 任务进度 key 前缀 **/
    private static final String taskAction = "subscribe.worker.methodDesc.";
    /** 过期时间 秒 **/
    private static final Integer taskActionExpiration = 60 * 60;

    @Resource
    private RedissonClient redissonClient;


    /**
     *@Description: 订阅任务进度
     *@Param: [taskCode, sessionId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/28
     */
    public void subscribeTaskAction(Long taskCode, String sessionId) {
//        redisCacheMgr.put(taskAction + taskCode, taskActionExpiration, sessionId);
    }

    /**
     *@Description: 取消任务进度订阅
     *@Param: [taskCode]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/28
     */
    public void unsubscribeTaskAction(Long taskCode) {
//        redisCacheMgr.remove(taskAction + taskCode);
    }

    /**
     *@Description: 获得订阅该任务进度的sessionId
     *@Param: [taskCode]
     *@Return: java.lang.String
     *@Author: wen
     *@Date: 2018/4/28
     */
    public String getSubscribeTaskActionSessionId(Long taskCode) {
//        return (String) redisCacheMgr.get(taskAction + taskCode);
        return "";
    }


}
