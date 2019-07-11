package com.testwa.distest.client.device.pool;

import com.testwa.distest.client.device.manager.DeviceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Created by wen on 13/08/2017.
 */
@Slf4j
public class DeviceManagerFactory extends BasePooledObjectFactory<DeviceManager> {
    private final String host;
    private final int port;
    private final String resourcePath;

    public DeviceManagerFactory(String host, int port, String resourcePath) {
        this.host = host;
        this.port = port;
        this.resourcePath = resourcePath;
    }

    @Override
    public DeviceManager create() {
        return new DeviceManager(this.host, this.port, this.resourcePath);
    }

    @Override
    public PooledObject<DeviceManager> wrap(DeviceManager executor) {
        return new DefaultPooledObject<>(executor);
    }

    @Override
    public void activateObject(PooledObject<DeviceManager> p)  {

    }

    @Override
    public void passivateObject(PooledObject<DeviceManager> pooledObject)  {
        pooledObject.getObject().reset();
    }

    /**
     * 对象销毁(clear时会触发）
     * @param pooledObject
     */
    @Override
    public void destroyObject(PooledObject<DeviceManager> pooledObject) {
        log.info("Device manager destory");
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
    public boolean validateObject(PooledObject<DeviceManager> p) {
        if (p.getObject() != null) {
            if (p.getObject().deviceIsOnline()) {
                return true;
            }
            p.getObject().deviceStart();
            return true;
        }
        return false;
    }

}
