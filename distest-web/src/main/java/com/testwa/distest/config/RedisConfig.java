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
    private RedisProperties properties;

    @Bean(name = "redisCacheDspMgr")
    public RedisCacheManager redisCacheDspMgr(){
        RedisCacheManager mgr = new RedisCacheManager();
        mgr.setEvictorCheckPeriodSeconds(properties.getEvictorCheckPeriodSeconds());
        mgr.setEvictorDelayCheckSeconds(properties.getEvictorDelayCheckSeconds());
        mgr.setEvictorFailedTimesToBeTickOut(properties.getEvictorFailedTimesToBeTickOut());
        mgr.setRetryTimes(properties.getRetryTimes());
        List<RedisClient> clientList = new ArrayList<>();
        properties.getClient().forEach(p -> {
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