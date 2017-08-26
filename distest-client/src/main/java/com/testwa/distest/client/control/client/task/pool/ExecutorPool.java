package com.testwa.distest.client.control.client.task.pool;

import com.testwa.distest.client.control.client.task.Executor;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by wen on 13/08/2017.
 */
public class ExecutorPool {
    private final GenericObjectPool<Executor> pool;


    /**
     * @param config
     */
    public ExecutorPool(String nodePath, String appiumPath, String agentWebUrl, String clientWebUrl, GenericObjectPoolConfig config) {
        pool = new GenericObjectPool<>(new ExecutorFactory(nodePath, appiumPath, agentWebUrl, clientWebUrl), config);
    }

    public Executor getService() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void release(Executor o) {
        pool.returnObject(o);
    }

}
