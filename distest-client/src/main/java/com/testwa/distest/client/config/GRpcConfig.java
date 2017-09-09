package com.testwa.distest.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.http.nio.reactor.IOReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by wen on 16/9/11.
 */
@Configuration
public class GRpcConfig {
    private static final Logger LOG = LoggerFactory.getLogger(GRpcConfig.class);
    @Autowired
    private Environment env;

    @Bean
    public ManagedChannel managedChannel() throws IOReactorException {
        return ManagedChannelBuilder
                .forAddress(env.getProperty("grpc.host"), Integer.parseInt(env.getProperty("grpc.port")))
                .usePlaintext(true)
                .build();
    }
}