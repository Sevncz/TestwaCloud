package com.testwa.distest.config;


import com.testwa.distest.redis.RedisCacheManager;
import com.testwa.distest.redis.RedisClient;
import com.testwa.distest.redis.config.RedisHAClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    @Autowired
    private RedisProperties redisProperties;

    @Bean(name = "redisCacheDspMgr")
    public RedisCacheManager redisCacheDspMgr(){
        RedisCacheManager mgr = new RedisCacheManager();
        mgr.setEvictorCheckPeriodSeconds(redisProperties.getEvictorCheckPeriodSeconds());
        mgr.setEvictorDelayCheckSeconds(redisProperties.getEvictorDelayCheckSeconds());
        mgr.setEvictorFailedTimesToBeTickOut(redisProperties.getEvictorFailedTimesToBeTickOut());
        mgr.setRetryTimes(redisProperties.getRetryTimes());
        List<RedisClient> clientList = new ArrayList<>();
        redisProperties.getClient().forEach(p -> {
            RedisHAClientConfig c = new RedisHAClientConfig();
            c.setCacheName(p.getName());
            c.setRedisAuthKey(p.getPassword());
            c.setRedisServerHost(p.getHost());
            c.setRedisServerPort(p.getPort());
            clientList.add(new RedisClient(c));
        });
        mgr.setClientList(clientList);
        return mgr;
    }


}  