package com.testwa.distest.client.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wen on 13/08/2017.
 */
@Slf4j
@Configuration
public class GrpcConfig {

//    @Autowired
//    private EurekaClient client;
    @Value("${grpc.host}")
    private String host;
    @Value("${grpc.port}")
    private Integer port;

    @Bean("serverChannel")
    public ManagedChannel serverChannel(){
//        final InstanceInfo instanceInfo = client.getNextServerFromEureka("distest-web", false);
        log.debug("GRPC: {}:{}", host, port);
        final ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();
        return channel;
    }
}
