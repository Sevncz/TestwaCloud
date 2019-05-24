package com.testwa.distest.client.device.driver;

import com.testwa.distest.client.DeviceClient;
import com.testwa.distest.client.device.manager.DeviceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wen
 * @create 2019-05-23 23:31
 */
public class IDeviceRemoteDriverCache {

    private static volatile ConcurrentHashMap<String, DeviceManager> deviceClientMap = new ConcurrentHashMap<>();

    public static void add(String serial, DeviceManager client){
        deviceClientMap.put(serial, client);
    }

    public static DeviceManager get(String serial){
        return deviceClientMap.get(serial);
    }

    public static List<DeviceManager> all(){
        return new ArrayList<>(deviceClientMap.values());
    }

    public static void remove(String serial){
        DeviceManager manager = deviceClientMap.get(serial);
        if(manager != null && manager.deviceIsOnline()){
            manager.destory();
            deviceClientMap.remove(serial);
        }
    }
}
