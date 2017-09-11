package com.testwa.distest.server.mvc.service.cache;

import com.testwa.distest.server.mvc.model.*;
import com.testwa.distest.server.mvc.repository.DeviceRepository;
import com.testwa.distest.server.mvc.repository.ProjectMemberRepository;
import com.testwa.distest.server.mvc.repository.UserDeviceHisRepository;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Created by wen on 24/06/2017.
 */
@Slf4j
@Service
public class WebCacheService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${user.history.size}")
    private Integer userHistorySize;

    public void userProjectHistory(User user, String projectId) {
        String key = String.format(CacheKeys.user_project_history, user.getId());

        List<String> projects = (List<String>) redisTemplate.opsForValue().get(key);
        if (projects == null) {
            projects = new ArrayList<>();
            projects.add(projectId);
        } else {
            Integer index = projects.indexOf(projectId);
            if (index != -1) {
                projects.remove(index);
            }
            projects.add(0, projectId);
        }
        if(projects.size() > userHistorySize)
            projects.remove(projects.size() - 1);
        redisTemplate.opsForValue().set(key, projects);
    }
}
