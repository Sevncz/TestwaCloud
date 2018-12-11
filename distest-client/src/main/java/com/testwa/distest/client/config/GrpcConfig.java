package com.testwa.distest.client.config;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wen on 13/08/2017.
 */
@Slf4j
@Configuration
@EnableEurekaClient
public class GrpcConfig {
    @Autowired
    private EurekaClient client;

    @Bean("serverChannel")
    public ManagedChannel serverChannel(){
//        log.debug("GRPC: {}:{}", host, port);
        final InstanceInfo instanceInfo = client.getNextServerFromEureka("grpc-server", false);
        log.info("Discovery GRPC: {}:{}", instanceInfo.getIPAddr(), instanceInfo.getPort());
        final ManagedChannel channel = ManagedChannelBuilder.forAddress(instanceInfo.getIPAddr(), instanceInfo.getPort())
                .usePlaintext()
                .build();
        return channel;
    }
}
