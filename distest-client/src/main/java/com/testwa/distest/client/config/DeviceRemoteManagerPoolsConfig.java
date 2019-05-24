package com.testwa.distest.client.config;

import com.testwa.distest.client.device.pool.DeviceManagerPool;
import com.testwa.distest.client.device.pool.DeviceManagerPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wen on 13/08/2017.
 */
@Configuration
public class DeviceRemoteManagerPoolsConfig {

    @Value("${distest.agent.resources}")
    private String resourcePath;
    @Value("${grpc.server.host}")
    private String grpcHost;
    @Value("${grpc.server.port}")
    private Integer grpcPort;

    @Bean("deviceManagerPool")
    public DeviceManagerPool deviceManagerPool(){
        DeviceManagerPoolConfig poolConfig = new DeviceManagerPoolConfig();
        return new DeviceManagerPool(grpcHost, grpcPort, resourcePath, poolConfig);
    }
}
