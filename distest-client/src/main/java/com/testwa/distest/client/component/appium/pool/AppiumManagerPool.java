package com.testwa.distest.client.component.appium.pool;

import com.testwa.distest.client.component.appium.AppiumManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by wen on 13/08/2017.
 */
@Slf4j
public class AppiumManagerPool {
    private final GenericObjectPool<AppiumManager> pool;


    /**
     * @param config
     */
    public AppiumManagerPool(String nodePath, String appiumPath, String clientWebUrl, GenericObjectPoolConfig config) {
        pool = new GenericObjectPool<>(new AppiumManagerFactory(nodePath, appiumPath, clientWebUrl), config);
    }

    public AppiumManager getManager() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            log.error("Borrow appium manager error", e);
            return null;
        }
    }

    public void release(AppiumManager o) {
        pool.returnObject(o);
    }

}
