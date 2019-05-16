package com.testwa.distest.client.android;

import com.testwa.distest.jadb.DeviceWatcher;
import com.testwa.distest.jadb.JadbConnection;
import com.testwa.distest.jadb.JadbDevice;
import com.testwa.distest.jadb.JadbException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author wen
 * @create 2019-05-15 18:43
 */
@Slf4j
public class JadbTools {
    private static JadbConnection jadbConnection;
    private static Thread deviceWatcherThread;

    static {
        jadbConnection = new JadbConnection();
        try {
            DeviceWatcher deviceWatcher = jadbConnection.createDeviceWatcher(new JadbDeviceDetectionListener());
            deviceWatcherThread = new Thread(deviceWatcher);
            deviceWatcherThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
    }

    public static void init() {
    }
}
