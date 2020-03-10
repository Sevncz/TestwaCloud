package com.testwa.distest.worker.core.appium.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by wen on 13/08/2017.
 */
public class AppiumManagerPoolConfig extends GenericObjectPoolConfig {

    public AppiumManagerPoolConfig() {
        setMinIdle(1);
        setMaxIdle(5);
        setMaxTotal(10);
        setMaxWaitMillis(-1);
    }
}
