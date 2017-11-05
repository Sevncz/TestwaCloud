package com.testwa.distest.server.service.cache.mgr;


import com.testwa.distest.redis.RedisCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TaskCacheMgr {

    private static final String device_execut_info = "device.execut.info.%s";

    @Autowired
    private RedisCacheManager redisCacheMgr;

    private String getKey(String deviceId){
        return String.format(device_execut_info, deviceId);
    }

    /**
     * 保存设备上报的当前的执行情况
     * @param taskId
     * @param deviceId
     * @param testcaseId
     * @param scriptId
     */
    public void saveExeInfo(String taskId, String deviceId, String testcaseId, String scriptId){
        Map<String, String> content = new HashMap<>();
        content.put("taskId", taskId);
        content.put("testcaseId", testcaseId);
        content.put("scriptId", scriptId);

        redisCacheMgr.lpush(getKey(deviceId), content);
    }

    /**
     * 根据设备Id获得设备当前执行情况
     * @param deviceId
     * @return
     */
    public String getExeInfoProgress(String deviceId) throws Exception {

        List<Object> recentOne = redisCacheMgr.lrange(getKey(deviceId), 0, 1, String.class);
        if(recentOne != null && recentOne.size() == 1){
            return (String) recentOne.get(0);
        }
        return null;
    }
    public Long getExeInfoSize(String deviceId) {
        return redisCacheMgr.llen(getKey(deviceId));
    }

}
