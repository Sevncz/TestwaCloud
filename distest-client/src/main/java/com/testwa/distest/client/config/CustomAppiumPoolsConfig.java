package com.testwa.distest.client.config;

import com.testwa.distest.client.component.appium.pool.CustomAppiumManagerPool;
import com.testwa.distest.client.component.appium.pool.CustomAppiumManagerPoolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PreDestroy;

/**
 * Created by wen on 13/08/2017.
 */
@Slf4j
@Configuration
public class CustomAppiumPoolsConfig {
    private CustomAppiumManagerPool pool;

    @Autowired
    private Environment env;

    @Bean("customAppiumManagerPool")
    public CustomAppiumManagerPool appiumPool() {
        String nodePath = env.getProperty("node.excute.path");
        String appiumPath = env.getProperty("appium.js.path");
//        String agentWebUrl = env.getProperty("cloud.web.url");
        String port = env.getProperty("server.port");
        String contextPath = env.getProperty("server.context-path");
        String clientWebUrl = String.format("http://127.0.0.1:%s%s/client", port, contextPath);
        CustomAppiumManagerPoolConfig poolConfig = new CustomAppiumManagerPoolConfig();
        pool = new CustomAppiumManagerPool(nodePath, appiumPath, clientWebUrl, poolConfig);
        initPool(1, poolConfig.getMaxIdle());
        log.info("启动 CustomAppiumManagerPool");
        return pool;
    }

    /**
     * 预先加载testObject对象到对象池中
     *
     * @param initialSize 初始化连接数
     * @param maxIdle     最大空闲连接数
     */
    private void initPool(int initialSize, int maxIdle) {
        if (initialSize <= 0) {
            return;
        }

        int size = Math.min(initialSize, maxIdle);
        for (int i = 0; i < size; i++) {
            try {
                pool.addObject();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    @PreDestroy
    public void destroy() {
        if (pool != null) {
            pool.close();
        }
    }
}
