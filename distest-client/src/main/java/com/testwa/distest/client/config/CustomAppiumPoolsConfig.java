package com.testwa.distest.client.config;

import com.testwa.distest.client.component.appium.pool.CustomAppiumManagerPool;
import com.testwa.distest.client.component.appium.pool.CustomAppiumManagerPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by wen on 13/08/2017.
 */
@Configuration
public class CustomAppiumPoolsConfig {

    @Autowired
    private Environment env;

    @Bean("customAppiumManagerPool")
    public CustomAppiumManagerPool appiumPool(){
        String nodePath = env.getProperty("node.excute.path");
        String appiumPath = env.getProperty("appium.js.path");
//        String agentWebUrl = env.getProperty("cloud.web.url");
        String port = env.getProperty("server.port");
        String contextPath = env.getProperty("server.context-path");
        String clientWebUrl = String.format("http://127.0.0.1:%s%s/client", port, contextPath);
        CustomAppiumManagerPoolConfig poolConfig = new CustomAppiumManagerPoolConfig();
        CustomAppiumManagerPool executorPool = new CustomAppiumManagerPool(nodePath, appiumPath, clientWebUrl, poolConfig);
        return executorPool;
    }
}
