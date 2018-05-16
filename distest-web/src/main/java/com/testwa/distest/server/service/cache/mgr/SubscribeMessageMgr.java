package com.testwa.distest.server.service.cache.mgr;

import com.testwa.core.redis.RedisCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class SubscribeMessageMgr {

    /** 任务进度 key 前缀 **/
    private static final String taskAction = "subscribe.factory.action.";
    /** 过期时间 秒 **/
    private static final Integer taskActionExpiration = 60 * 60;

    @Autowired
    private RedisCacheManager redisCacheMgr;


    /**
     *@Description: 订阅任务进度
     *@Param: [taskId, sessionId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/28
     */
    public void subscribeTaskAction(Long taskId, String sessionId) {
        redisCacheMgr.put(taskAction + taskId, taskActionExpiration, sessionId);
    }

    /**
     *@Description: 取消任务进度订阅
     *@Param: [taskId]
     *@Return: void
     *@Author: wen
     *@Date: 2018/4/28
     */
    public void unsubscribeTaskAction(Long taskId) {
        redisCacheMgr.remove(taskAction + taskId);
    }

    /**
     *@Description: 获得订阅该任务进度的sessionId
     *@Param: [taskId]
     *@Return: java.lang.String
     *@Author: wen
     *@Date: 2018/4/28
     */
    public String getSubscribeTaskActionSessionId(Long taskId) {
        return (String) redisCacheMgr.get(taskAction + taskId);
    }


}
