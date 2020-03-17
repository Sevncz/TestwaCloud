package com.testwa.distest.client.component.appium.pool;

import com.testwa.distest.client.component.appium.manager.CustomAppiumManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by wen on 13/08/2017.
 */
@Slf4j
public class CustomAppiumManagerPool {
    private final GenericObjectPool<CustomAppiumManager> pool;


    /**
     * @param config
     */
    public CustomAppiumManagerPool(String nodePath, String appiumPath, String clientWebUrl, GenericObjectPoolConfig config) {
        pool = new GenericObjectPool<>(new CustomAppiumManagerFactory(nodePath, appiumPath, clientWebUrl), config);
    }

    public synchronized CustomAppiumManager getManager() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            log.error("Borrow appium manager error", e);
            return null;
        }
    }

    public void release(CustomAppiumManager o) {
        pool.returnObject(o);
    }


    public void addObject() {
        try {
            pool.addObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            pool.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
