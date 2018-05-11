package com.testwa.distest.client.config;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.testwa.distest.client.component.appium.pool.AppiumManagerPool;
import com.testwa.distest.client.component.appium.pool.AppiumManagerPoolConfig;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by wen on 13/08/2017.
 */
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

        final ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();
        return channel;
    }
}
