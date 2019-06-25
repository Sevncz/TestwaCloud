package com.testwa.distest.client.android;

import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.event.DeviceStatusChangeEvent;
import com.testwa.distest.jadb.JadbDevice;
import com.testwa.distest.jadb.JadbException;
import io.rpc.testwa.device.DeviceStatusChangeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author wen
 * @create 2019-05-15 19:12
 */
@Slf4j
public class JadbDeviceManager {
    public final static ConcurrentHashMap<String, JadbDevice> jadbDeviceMap = new ConcurrentHashMap<>();
    public final static ApplicationContext context = ApplicationContextUtil.getApplicationContext();

    public synchronized static void putAll(List<JadbDevice> devices) {
        devices.forEach( d-> {
            JadbDevice device = jadbDeviceMap.get(d.getSerial());
            if(device != null) {
                try {
                    if(d.getState().equals(device.getState())) {
                        changeState(d);
                    }
                } catch (IOException | JadbException e) {
                    changeState(d);
                }
            }else{
                changeState(d);
            }
        });

        getJadbDeviceList().forEach( d -> {
            try {
                d.getState();
            } catch (IOException | JadbException e) {
                onDisconnect(d);
            }
        });
    }

    public static void changeState(JadbDevice d){
        try {
            switch (d.getState()) {
                case Device:
                    // 初始化设备
                    onDevice(d);
                    break;
                case Offline:
                    // 关闭设备
                    onOffline(d);
                    break;
                case BootLoader:
                    onBootLoader(d);
                    break;
                case Recovery:
                    onRecovery(d);
                    break;
                case Unauthorized:
                    onUnauthorized(d);
                    break;
                case Authorizing:
                    onAuthorizing(d);
                    break;
                case Connecting:
                    onConnecting(d);
                    break;
                case Sideload:
                    onSideload(d);
                    break;
                default:
                    break;
            }
        } catch (IOException | JadbException e) {
            onDisconnect(d);
        }
    }

    public static List<JadbDevice> getJadbDeviceList() {
        return new ArrayList<>(jadbDeviceMap.values());
    }

    public static JadbDevice getJadbDevice(String deviceId) {
        return jadbDeviceMap.get(deviceId);
    }

    /**
     * @Description: 初始化设备
     * @Param: [deviceId]
     * @Return: void
     * @Author wen
     * @Date 2019/5/15 19:54
     */
    public static void onDevice(JadbDevice jadbDevice){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, jadbDevice, DeviceStatusChangeRequest.LineStatus.ONLINE));

    }

    public static void onOffline(JadbDevice jadbDevice){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, jadbDevice, DeviceStatusChangeRequest.LineStatus.OFFLINE));

    }

    public static void onBootLoader(JadbDevice jadbDevice){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, jadbDevice, DeviceStatusChangeRequest.LineStatus.BOOTLOADER));

    }

    public static void onRecovery(JadbDevice jadbDevice){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, jadbDevice, DeviceStatusChangeRequest.LineStatus.RECOVERY));

    }

    public static void onUnauthorized(JadbDevice jadbDevice){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, jadbDevice, DeviceStatusChangeRequest.LineStatus.UNAUTHORIZED));

    }

    public static void onAuthorizing(JadbDevice jadbDevice){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, jadbDevice, DeviceStatusChangeRequest.LineStatus.AUTHORIZING));

    }

    public static void onConnecting(JadbDevice jadbDevice){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, jadbDevice, DeviceStatusChangeRequest.LineStatus.CONNECTING));
    }

    public static void onSideload(JadbDevice jadbDevice){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, jadbDevice, DeviceStatusChangeRequest.LineStatus.SIDELOAD));
    }

    public static void onDisconnect(JadbDevice jadbDevice){
        jadbDeviceMap.remove(jadbDevice.getSerial());
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, jadbDevice, DeviceStatusChangeRequest.LineStatus.DISCONNECTED));
    }
}
