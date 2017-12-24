package com.testwa.distest.client.control.client.grpc.pool;

;
import com.testwa.distest.client.control.client.grpc.GClient;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Created by wen on 13/08/2017.
 */
public class GClientFactory extends BasePooledObjectFactory<GClient> {
    private String host;
    private Integer port;

    public GClientFactory(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public GClient create() throws Exception {
        return new GClient(this.host, this.port);
    }

    @Override
    public PooledObject<GClient> wrap(GClient GClient) {
        return new DefaultPooledObject<>(GClient);
    }

    @Override
    public void activateObject(PooledObject<GClient> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<GClient> pooledObject) throws Exception {

        pooledObject.getObject().reset();
    }

    /**
     * 对象销毁(clear时会触发）
     * @param pooledObject
     */
    @Override
    public void destroyObject(PooledObject<GClient> pooledObject) {
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
    public boolean validateObject(PooledObject<GClient> p) {
        if (p.getObject() != null) {
            if (p.getObject().isShutdown()) {
                return false;
            }
            p.getObject().start();
            return true;
        }
        return false;
    }

}
