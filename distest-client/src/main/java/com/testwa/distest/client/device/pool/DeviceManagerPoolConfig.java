package com.testwa.distest.client.device.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by wen on 2019/5/23
 */
public class DeviceManagerPoolConfig extends GenericObjectPoolConfig {

    public DeviceManagerPoolConfig() {
        setMinIdle(5);
        setMaxIdle(30);
        setMaxTotal(50);
        setMaxWaitMillis(-1);
    }
}
