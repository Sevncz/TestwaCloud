package com.testwa.distest.client.component.appium.pool;

import com.testwa.distest.client.component.appium.AppiumManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Created by wen on 13/08/2017.
 */
@Slf4j
public class AppiumManagerFactory extends BasePooledObjectFactory<AppiumManager> {
    private String nodePath;
    private String appiumPath;
    private String clientWebUrl;

    public AppiumManagerFactory(String nodePath, String appiumPath, String clientWebUrl) {
        this.nodePath = nodePath;
        this.appiumPath = appiumPath;
        this.clientWebUrl = clientWebUrl;
    }

    @Override
    public AppiumManager create() throws Exception {
        log.info("Appium manager create");
        return new AppiumManager(this.nodePath, this.appiumPath, this.clientWebUrl);
    }

    @Override
    public PooledObject<AppiumManager> wrap(AppiumManager executor) {
        return new DefaultPooledObject<>(executor);
    }

    @Override
    public void activateObject(PooledObject<AppiumManager> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<AppiumManager> pooledObject) throws Exception {

        pooledObject.getObject().reset();
    }

    /**
     * 对象销毁(clear时会触发）
     * @param pooledObject
     */
    @Override
    public void destroyObject(PooledObject<AppiumManager> pooledObject) {
        log.info("Appium manager destory");
        pooledObject.getObject().destory();
        pooledObject.markAbandoned();
    }

    /**
     * 验证对象有效性
     *
     * @param p
     * @return
     */
    @Override
    public boolean validateObject(PooledObject<AppiumManager> p) {
        if (p.getObject() != null) {
            if (p.getObject().appiumIsRunning()) {
                return true;
            }
            p.getObject().appiumStart();
            return true;
        }
        return false;
    }

}
