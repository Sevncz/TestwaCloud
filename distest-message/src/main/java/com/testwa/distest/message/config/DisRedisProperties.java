package com.testwa.distest.message.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wen on 25/10/2017.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dis-redis")
public class DisRedisProperties {

    private int evictorDelayCheckSeconds;
    private int evictorCheckPeriodSeconds;
    private int evictorFailedTimesToBeTickOut;
    private int retryTimes;

    private List<GroupConfig> group = new ArrayList();

    @Data
    public static class GroupConfig{
        private List<ClientConfig> client = new ArrayList<>();
    }

    @Data
    public static class ClientConfig{
        private String name;
        private String host;
        private int port;
        private String password;
        private int timeout;
    }
}
