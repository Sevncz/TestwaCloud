package com.testwa.distest.server.service.project.service;

import com.testwa.distest.common.util.WebUtil;
import com.testwa.distest.redis.RedisCacheManager;
import com.testwa.distest.server.entity.Project;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wen on 24/06/2017.
 */
@Slf4j
@Service
public class ViewMgr {

    @Autowired
    private RedisCacheManager redisCacheMgr;
    @Value("${user.history.size}")
    private Integer userHistorySize;
    static final String user_project_history = "history.project.%s";

    public void setRecentViewProject(Project project) throws Exception {
        String key = String.format(user_project_history, WebUtil.getCurrentUsername());
        List<Object> projects = redisCacheMgr.lrange(key, 0, -1, Project.class);
        if(projects.contains(project)){
            redisCacheMgr.lrem(key, 0, project);
        }
        // 保存记录
        redisCacheMgr.lpush(key, project);
        // 裁剪
        redisCacheMgr.ltrim(key, 0, userHistorySize);

    }

    public List<Project> getRecentViewProject(String username) throws Exception {
        String key = String.format(user_project_history, username);
        List<Object> objs = redisCacheMgr.lrange(key, 0, -1, Project.class);
        List<Project> projects = new ArrayList<>();
        if(objs != null ){
            objs.forEach(o -> {
                projects.add((Project) o);
            });
        }
        return projects;
    }
}
