package com.testwa.distest.client.config;

import com.testwa.distest.client.component.appium.pool.AppiumManagerPool;
import com.testwa.distest.client.component.appium.pool.AppiumManagerPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by wen on 13/08/2017.
 */
@Configuration
public class AppiumPoolsConfig {

    @Autowired
    private Environment env;

    @Bean("appiumManagerPool")
    public AppiumManagerPool appiumPool(){
        String nodePath = env.getProperty("node.excute.path");
        String appiumPath = env.getProperty("appium.js.path");
//        String agentWebUrl = env.getProperty("cloud.web.url");
        String port = env.getProperty("server.port");
        String contextPath = env.getProperty("server.context-path");
        String clientWebUrl = String.format("http://127.0.0.1:%s%s/client", port, contextPath);
        AppiumManagerPoolConfig poolConfig = new AppiumManagerPoolConfig();
        AppiumManagerPool executorPool = new AppiumManagerPool(nodePath, appiumPath, clientWebUrl, poolConfig);
        return executorPool;
    }
}
