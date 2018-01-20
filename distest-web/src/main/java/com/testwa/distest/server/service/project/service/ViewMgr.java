package com.testwa.distest.server.service.project.service;

import com.testwa.distest.common.util.WebUtil;
import com.testwa.core.redis.RedisCacheManager;
import com.testwa.distest.server.entity.Project;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Created by wen on 24/06/2017.
 */
@Log4j2
@Service
public class ViewMgr {

    @Autowired
    private RedisCacheManager redisCacheMgr;
    @Value("${user.history.size}")
    private Integer userHistorySize;
    static final String user_project_history = "history.project.%s";

    public void setRecentViewProject(Long projectId) throws Exception {
        String key = String.format(user_project_history, WebUtil.getCurrentUsername());
        redisCacheMgr.lrem(key, 0, projectId+"");
        // 保存记录
        redisCacheMgr.lpush(key, projectId);
        // 裁剪
        redisCacheMgr.ltrim(key, 0, userHistorySize-1);

    }

    public List<Long> getRecentViewProject(String username) throws Exception {
        String key = String.format(user_project_history, username);
        return  (List<Long>) redisCacheMgr.lrange(key, 0, -1, Long.class);
    }
}
