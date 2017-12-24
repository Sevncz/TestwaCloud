package com.testwa.distest.client.control.client.grpc.pool;

import com.testwa.distest.client.control.client.grpc.GClient;
import com.testwa.distest.client.control.client.task.Executor;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by wen on 13/08/2017.
 */
public class GClientPool {
    private final GenericObjectPool<GClient> pool;


    /**
     * @param config
     */
    public GClientPool(String host, Integer port, GenericObjectPoolConfig config) {
        pool = new GenericObjectPool<>(new GClientFactory(host, port), config);
    }

    public GClient getClient() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void release(GClient o) {
        pool.returnObject(o);
    }

}
