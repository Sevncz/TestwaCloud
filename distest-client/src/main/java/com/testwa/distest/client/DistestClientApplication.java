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

/**
 * Created by wen on 16/8/14.
 */
//@EnableDiscoveryClient
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableScheduling
@EnableAsync
public class DistestClientApplication extends AsyncConfigurerSupport {

    public static void main(String[] args) {

        SpringApplication.run(DistestClientApplication.class, args);
    }

}
