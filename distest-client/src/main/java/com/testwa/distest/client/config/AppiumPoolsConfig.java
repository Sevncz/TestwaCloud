package com.testwa.distest.client.config;

import com.testwa.distest.client.control.client.task.pool.ExecutorPool;
import com.testwa.distest.client.control.client.task.pool.ExecutorPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by wen on 13/08/2017.
 */
@Configuration
public class AppiumPoolsConfig {
    private static final Logger LOG = LoggerFactory.getLogger(AppiumPoolsConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public ExecutorPool appiumPool(){
        String nodePath = env.getProperty("node.excute.path");
        String appiumPath = env.getProperty("appium.js.path");
        String agentWebUrl = env.getProperty("agent.web.url");
        String port = env.getProperty("server.port");
        String contextPath = env.getProperty("server.context-path");
        String clientWebUrl = String.format("http://127.0.0.1:%s%s/client", port, contextPath);
        ExecutorPoolConfig poolConfig = new ExecutorPoolConfig();
        ExecutorPool executorPool = new ExecutorPool(nodePath, appiumPath, agentWebUrl, clientWebUrl, poolConfig);
        return executorPool;
    }
}
