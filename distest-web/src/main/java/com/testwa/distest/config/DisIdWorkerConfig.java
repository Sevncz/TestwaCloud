package com.testwa.distest.config;

import com.testwa.core.tools.SnowflakeIdWorker;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "dis-id")
public class DisIdWorkerConfig {

    private long workerId;

    @Bean("taskIdWorker")
    public SnowflakeIdWorker taskIdWorker(){
        return new SnowflakeIdWorker(workerId, 0);
    }
    @Bean("commonIdWorker")
    public SnowflakeIdWorker commonIdWorker(){
        return new SnowflakeIdWorker(workerId, 1);
    }
}
