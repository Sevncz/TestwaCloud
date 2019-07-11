package com.testwa.distest.client.android;

import com.testwa.distest.jadb.DeviceDetectionListener;
import com.testwa.distest.jadb.JadbDevice;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author wen
 * @create 2019-05-15 18:50
 */
@Slf4j
public class JadbDeviceDetectionListener implements DeviceDetectionListener {
    @Override
    public void onDetect(List<JadbDevice> devices) {
        JadbDeviceManager.putAll(devices);
    }

    @Override
    public void onException(Exception e) {
        log.info("device error", e);
    }
}
