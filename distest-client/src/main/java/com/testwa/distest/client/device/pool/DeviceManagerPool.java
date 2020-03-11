package com.testwa.distest.client.device.pool;

import com.testwa.distest.client.android.JadbDeviceManager;
import com.testwa.distest.client.device.manager.DeviceManager;
import com.testwa.distest.jadb.JadbDevice;
import io.rpc.testwa.device.DeviceType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wen on 2019/5/23
 */
@Slf4j
public class DeviceManagerPool {
    private final ConcurrentHashMap<String, DeviceManager> borrowManger = new ConcurrentHashMap<>();
    private final GenericObjectPool<DeviceManager> pool;

    /**
     * @param config
     */
    public DeviceManagerPool(String host, int port, String resourcePath, GenericObjectPoolConfig config) {
        pool = new GenericObjectPool<>(new DeviceManagerFactory(host, port, resourcePath), config);
    }

    public DeviceManager getInitialManager(String deviceId) {
        if(borrowManger.containsKey(deviceId)) {
            return borrowManger.get(deviceId);
        }
        return null;
    }

    public DeviceManager getManager(String deviceId, DeviceType deviceType) {
        DeviceManager manager = null;
        try {
            if(borrowManger.containsKey(deviceId)) {
                return borrowManger.get(deviceId);
            }
            if(DeviceType.ANDROID.equals(deviceType)){
                JadbDevice jadbDevice = JadbDeviceManager.getJadbDevice(deviceId);
                if(jadbDevice == null) {
                    throw new Exception("Android 设备还未初始化完成");
                }
            }
            manager = pool.borrowObject();
            if(manager != null) {
                manager.init(deviceId, deviceType);
                borrowManger.put(deviceId, manager);
            }
            return manager;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[DeviceManager 初始化失败] -e-{}-e-", deviceId, e.getMessage());
            if(manager != null) {
                pool.returnObject(manager);
            }
            return null;
        }
    }

    public boolean hasExist(String deviceId) {
        return borrowManger.containsKey(deviceId);
    }

    public void release(DeviceManager o) {
        pool.returnObject(o);
        borrowManger.remove(o.getDeviceId());
    }

    public void release(String deviceId) {
        DeviceManager o = borrowManger.remove(deviceId);
        if(o != null) {
            o.destory();
            pool.returnObject(o);
        }
    }

}
