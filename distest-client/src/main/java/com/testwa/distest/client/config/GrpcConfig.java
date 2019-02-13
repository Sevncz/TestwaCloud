package com.testwa.distest.client.config;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wen on 13/08/2017.
 */
//@Slf4j
//@Configuration
//@EnableEurekaClient
public class GrpcConfig {
//    @Autowired
//    @Qualifier(value = "eurekaClient")
//    private EurekaClient client;
//    @Value("${grpc.server.name}")
//    private String grpcServerName;
//    @Value("${grpc.host}")
//    private String grpcHost;
//    @Value("${grpc.port}")
//    private Integer grpcPort;
//
//    @Bean("serverChannel")
//    public ManagedChannel serverChannel(){
//        final InstanceInfo instanceInfo = client.getNextServerFromEureka(grpcServerName, false);
//        log.info("Discovery GRPC: {}:{}", instanceInfo.getIPAddr(), instanceInfo.getPort());
//        final ManagedChannel channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
//                .usePlaintext()
//                .build();
//        return channel;
//    }
}
