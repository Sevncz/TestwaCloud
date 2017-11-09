package com.testwa.distest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wen on 25/10/2017.
 */
@Data
@Configuration
//@PropertySource("classpath:redis-config.yml")
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {

    private int evictorDelayCheckSeconds;
    private int evictorCheckPeriodSeconds;
    private int evictorFailedTimesToBeTickOut;
    private int retryTimes;

    private List<ClientConfig> client = new ArrayList<>();

    @Data
    public static class ClientConfig{
        private String name;
        private String host;
        private int port;
        private String password;
        private int timeout;
    }

}
