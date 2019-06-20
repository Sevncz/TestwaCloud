package com.testwa.distest.client.task;

import com.sun.jna.Platform;
import com.testwa.distest.client.android.JadbDeviceManager;
import com.testwa.distest.client.component.appium.utils.Config;
import com.testwa.distest.client.device.manager.DeviceManager;
import com.testwa.distest.client.device.pool.DeviceManagerPool;
import com.testwa.distest.client.ios.IOSDeviceUtil;
import com.testwa.distest.client.service.GrpcClientService;
import com.testwa.distest.jadb.JadbDevice;
import io.rpc.testwa.device.DeviceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by wen on 16/9/4.
 */
@Slf4j
@Component
public class CronScheduled {
    private ExecutorService deviceExecutor = Executors.newCachedThreadPool();

    @Autowired
    private GrpcClientService grpcClientService;
    @Autowired
    private Environment env;
    @Autowired
    private DeviceManagerPool deviceManagerPool;

    /**
     *@Description: android设备在线情况的补充检查
     *@Param: []
     *@Return: void
     *@Author: wen
     *@Date: 2018/5/8
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 1000)
    public void androidInit() {
        Config.setEnv(env);
        List<JadbDevice> devices = JadbDeviceManager.getJadbDeviceList();
        List<Future<DeviceManager>> resultList = new ArrayList<>();
        devices.forEach(d -> {
            Future<DeviceManager> future = deviceExecutor.submit(new DeviceManagerTask(d.getSerial(), DeviceType.ANDROID));
            resultList.add(future);
        });

        //遍历任务的结果
        for (Future<DeviceManager> fs : resultList){
            while(!fs.isDone()){

            }
        }
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void iOSInit() {
        Config.setEnv(env);

        if(Platform.isMac()) {
            List<String> udids = IOSDeviceUtil.getUDID();
            log.debug("udids {}", udids.toString());
            List<Future<DeviceManager>> resultList = new ArrayList<>();
            udids.forEach(udid -> {
                IOSDeviceUtil.addOnline(udid);
                Future<DeviceManager> future = deviceExecutor.submit(new DeviceManagerTask(udid, DeviceType.IOS));
                resultList.add(future);
            });
            //遍历任务的结果
            for (Future<DeviceManager> fs : resultList){
                while(!fs.isDone()){

                }
            }
        }
    }

    class DeviceManagerTask implements Callable<DeviceManager> {
        private String deviceId;
        private DeviceType type;

        public DeviceManagerTask(String deviceId, DeviceType type) {
            this.deviceId = deviceId;
            this.type = type;
        }

        @Override
        public DeviceManager call() throws Exception {
            return deviceManagerPool.getManager(deviceId, type);
        }
    }

    @Scheduled(fixedDelay = 3000)
    public void iOSClear() {
        // 2个间隔时间都检查到离线，才离线
        IOSDeviceUtil.ONLINE_UDID.forEach(udid -> {
            if(!IOSDeviceUtil.isOnline(udid)){
                log.warn("iOS 设备 {} 离线", udid);
                IOSDeviceUtil.removeOnline(udid);
                deviceManagerPool.release(udid);
            }
        });
    }

}
