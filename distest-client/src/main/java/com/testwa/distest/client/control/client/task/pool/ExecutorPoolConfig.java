package com.testwa.distest.client.control.client.task.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by wen on 13/08/2017.
 */
public class ExecutorPoolConfig extends GenericObjectPoolConfig {

    public ExecutorPoolConfig() {
        setMinIdle(1);
        setMaxIdle(5);
        setMaxWaitMillis(-1);
    }
}
