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
                        jadbDeviceMap.put(d.getSerial(), d);
                    }
                } catch (IOException | JadbException e) {
                    changeState(d);
                    jadbDeviceMap.remove(d.getSerial());
                }
            }else{
                changeState(d);
                jadbDeviceMap.put(d.getSerial(), d);
            }
        });

        getJadbDeviceList().forEach( d -> {
            try {
                d.getState();
            } catch (IOException | JadbException e) {
                onDisconnect(d.getSerial());
                jadbDeviceMap.remove(d.getSerial());
            }
        });
    }

    public static void changeState(JadbDevice d){
        try {
            switch (d.getState()) {
                case Device:
                    // 初始化设备
                    onDevice(d.getSerial());
                    break;
                case Offline:
                    // 关闭设备
                    onOffline(d.getSerial());
                    break;
                case BootLoader:
                    onBootLoader(d.getSerial());
                    break;
                case Recovery:
                    onRecovery(d.getSerial());
                    break;
                case Unauthorized:
                    onUnauthorized(d.getSerial());
                    break;
                case Authorizing:
                    onAuthorizing(d.getSerial());
                    break;
                case Connecting:
                    onConnecting(d.getSerial());
                    break;
                case Sideload:
                    onSideload(d.getSerial());
                    break;
                default:
                    break;
            }
        } catch (IOException | JadbException e) {
            onDisconnect(d.getSerial());
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
    public static void onDevice(String deviceId){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, deviceId, DeviceStatusChangeRequest.LineStatus.ONLINE));

    }

    public static void onOffline(String deviceId){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, deviceId, DeviceStatusChangeRequest.LineStatus.OFFLINE));

    }

    public static void onBootLoader(String deviceId){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, deviceId, DeviceStatusChangeRequest.LineStatus.BOOTLOADER));

    }

    public static void onRecovery(String deviceId){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, deviceId, DeviceStatusChangeRequest.LineStatus.RECOVERY));

    }

    public static void onUnauthorized(String deviceId){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, deviceId, DeviceStatusChangeRequest.LineStatus.UNAUTHORIZED));

    }

    public static void onAuthorizing(String deviceId){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, deviceId, DeviceStatusChangeRequest.LineStatus.AUTHORIZING));

    }

    public static void onConnecting(String deviceId){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, deviceId, DeviceStatusChangeRequest.LineStatus.CONNECTING));
    }

    public static void onSideload(String deviceId){
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, deviceId, DeviceStatusChangeRequest.LineStatus.SIDELOAD));
    }

    public static void onDisconnect(String deviceId){
        jadbDeviceMap.remove(deviceId);
        context.publishEvent(new DeviceStatusChangeEvent(JadbDeviceManager.class, deviceId, DeviceStatusChangeRequest.LineStatus.DISCONNECTED));
    }
}
