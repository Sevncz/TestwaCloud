package com.testwa.distest.client.component.appium.pool;

import com.testwa.distest.client.component.appium.manager.CustomAppiumManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Created by wen on 13/08/2017.
 */
@Slf4j
public class CustomAppiumManagerFactory extends BasePooledObjectFactory<CustomAppiumManager> {
    private String nodePath;
    private String appiumPath;
    private String clientWebUrl;

    public CustomAppiumManagerFactory(String nodePath, String appiumPath, String clientWebUrl) {
        this.nodePath = nodePath;
        this.appiumPath = appiumPath;
        this.clientWebUrl = clientWebUrl;
    }

    @Override
    public CustomAppiumManager create() throws Exception {
        log.info("Appium manager create");
        return new CustomAppiumManager(this.nodePath, this.appiumPath, this.clientWebUrl);
    }

    @Override
    public PooledObject<CustomAppiumManager> wrap(CustomAppiumManager executor) {
        return new DefaultPooledObject<>(executor);
    }

    @Override
    public void activateObject(PooledObject<CustomAppiumManager> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<CustomAppiumManager> pooledObject) throws Exception {

        pooledObject.getObject().reset();
    }

    /**
     * 对象销毁(clear时会触发）
     * @param pooledObject
     */
    @Override
    public void destroyObject(PooledObject<CustomAppiumManager> pooledObject) {
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
    public boolean validateObject(PooledObject<CustomAppiumManager> p) {
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
