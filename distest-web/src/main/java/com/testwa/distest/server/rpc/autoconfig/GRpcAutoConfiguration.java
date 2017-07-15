package com.testwa.distest.server.rpc.autoconfig;

import com.testwa.distest.server.rpc.GRpcServerRunner;
import com.testwa.distest.server.rpc.GRpcService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GRpcServerProperties.class)
public class GRpcAutoConfiguration {
    @Bean
    @ConditionalOnBean(annotation = GRpcService.class)
    public GRpcServerRunner grpcServerRunner(){
        return new GRpcServerRunner();
    }
}