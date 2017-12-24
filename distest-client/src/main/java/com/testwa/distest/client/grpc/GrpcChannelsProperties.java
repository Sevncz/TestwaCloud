package com.testwa.distest.client.grpc;

import com.google.common.collect.Maps;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;

import lombok.Data;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Data
@ConfigurationProperties("grpc")
public class GrpcChannelsProperties {

    @NestedConfigurationProperty
    private Map<String, GrpcChannelProperties> client = Maps.newHashMap();

    public GrpcChannelProperties getChannel(String name) {
        GrpcChannelProperties grpcChannelProperties = client.get(name);
        if (grpcChannelProperties == null) {
            grpcChannelProperties = GrpcChannelProperties.DEFAULT;
        }
        return grpcChannelProperties;
    }
}
