package com.testwa.distest.client;

import com.testwa.distest.client.config.PortConfig;
import com.testwa.distest.client.control.port.ApkPortProvider;
import com.testwa.distest.client.control.port.AppiumPortProvider;
import com.testwa.distest.client.control.port.ScreenPortProvider;
import com.testwa.distest.client.control.port.TouchPortProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Created by wen on 16/8/14.
 */
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableScheduling
@EnableAsync
public class TestwaClientApplication extends AsyncConfigurerSupport {

    public static void main(String[] args) {
        ScreenPortProvider.init(PortConfig.screenPortStart, PortConfig.screenPortEnd);
        TouchPortProvider.init(PortConfig.touchPortStart, PortConfig.touchPortEnd);
        ApkPortProvider.init(PortConfig.apkPortStart, PortConfig.apkPortEnd);
        AppiumPortProvider.init(PortConfig.appiumPortStart, PortConfig.appiumPortEnd);

        SpringApplication.run(TestwaClientApplication.class, args);
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("TestwaAgentLookup-");
        executor.initialize();
        return executor;
    }


}
