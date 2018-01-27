package com.testwa.distest.client.component.appium.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by wen on 13/08/2017.
 */
public class AppiumManagerPoolConfig extends GenericObjectPoolConfig {

    public AppiumManagerPoolConfig() {
        setMinIdle(5);
        setMaxIdle(10);
        setMaxTotal(20);
        setMaxWaitMillis(-1);
    }
}
