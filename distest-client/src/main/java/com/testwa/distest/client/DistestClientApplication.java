package com.testwa.distest.client;

import com.testwa.distest.client.config.PortConfig;
import com.testwa.distest.client.component.port.ApkPortProvider;
import com.testwa.distest.client.component.port.AppiumPortProvider;
import com.testwa.distest.client.component.port.MinicapPortProvider;
import com.testwa.distest.client.component.port.MinitouchPortProvider;
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
public class DistestClientApplication extends AsyncConfigurerSupport {

    public static void main(String[] args) {
        MinicapPortProvider.init(PortConfig.screenPortStart, PortConfig.screenPortEnd);
        MinitouchPortProvider.init(PortConfig.touchPortStart, PortConfig.touchPortEnd);
        ApkPortProvider.init(PortConfig.apkPortStart, PortConfig.apkPortEnd);
        AppiumPortProvider.init(PortConfig.appiumPortStart, PortConfig.appiumPortEnd);

        SpringApplication.run(DistestClientApplication.class, args);
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(30);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("WaLookup-");
        executor.initialize();
        return executor;
    }


}
