package com.testwa.distest.client.control.client.grpc.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by wen on 13/08/2017.
 */
public class GClientPoolConfig extends GenericObjectPoolConfig {

    public GClientPoolConfig() {
        setMinIdle(1);
        setMaxIdle(5);
        setMaxWaitMillis(-1);
    }
}
