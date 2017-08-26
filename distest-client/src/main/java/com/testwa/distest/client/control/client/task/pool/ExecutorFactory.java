package com.testwa.distest.client.control.client.task.pool;

import com.testwa.distest.client.control.client.task.Executor;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Created by wen on 13/08/2017.
 */
public class ExecutorFactory extends BasePooledObjectFactory<Executor> {
    private String nodePath;
    private String appiumPath;
    private String agentWebUrl;
    private String clientWebUrl;

    public ExecutorFactory(String nodePath, String appiumPath, String agentWebUrl, String clientWebUrl) {
        this.nodePath = nodePath;
        this.appiumPath = appiumPath;
        this.agentWebUrl = agentWebUrl;
        this.clientWebUrl = clientWebUrl;
    }

    @Override
    public Executor create() throws Exception {
        return new Executor(this.nodePath, this.appiumPath, this.agentWebUrl, this.clientWebUrl);
    }

    @Override
    public PooledObject<Executor> wrap(Executor executor) {
        return new DefaultPooledObject<>(executor);
    }

    @Override
    public void activateObject(PooledObject<Executor> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<Executor> pooledObject) throws Exception {

        pooledObject.getObject().reset();
    }

    /**
     * 对象销毁(clear时会触发）
     * @param pooledObject
     */
    @Override
    public void destroyObject(PooledObject<Executor> pooledObject) {
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
    public boolean validateObject(PooledObject<Executor> p) {
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
