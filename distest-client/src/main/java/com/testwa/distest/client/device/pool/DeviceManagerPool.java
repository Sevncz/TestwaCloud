package com.testwa.distest.client.device.pool;

import com.testwa.distest.client.device.manager.DeviceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by wen on 2019/5/23
 */
@Slf4j
public class DeviceManagerPool {
    private final GenericObjectPool<DeviceManager> pool;


    /**
     * @param config
     */
    public DeviceManagerPool(String host, int port, String userToken, String resourcePath, GenericObjectPoolConfig config) {
        pool = new GenericObjectPool<>(new DeviceManagerFactory(host, port, resourcePath, userToken), config);
    }

    public synchronized DeviceManager getManager() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            log.error("Borrow device manager error", e);
            return null;
        }
    }

    public void release(DeviceManager o) {
        pool.returnObject(o);
    }

}
