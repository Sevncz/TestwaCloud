package com.testwa.distest.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by wen on 16/8/14.
 */
@SpringBootApplication(exclude = MongoAutoConfiguration.class)
@EnableScheduling
public class TestwaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestwaClientApplication.class, args);
    }

}
