package com.testwa.distest.client.component.executor.uiautomator2;

import com.testwa.distest.client.android.ADBCommandUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppiumUi2ServerDaemon implements Runnable, Closeable {
    private boolean isStrat = false;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private String deviceId;
    private Ui2ServerForAppium ui2ServerForAppium;

    public AppiumUi2ServerDaemon(String deviceId) {
        this.deviceId = deviceId;
        this.isRunning.set(true);
    }

    @Override
    public void run() {
        while(isRunning.get()) {
            boolean isExsit = false;
            String ps_output = ADBCommandUtils.command(deviceId, new String[]{"ps"});
            String[] outs = ps_output.split("\n");
            for(String line : outs) {
                if(line.contains("io.appium.uiautomator2.server")) {
                    isStrat = true;
                    isExsit = true;
                }
            }
            if(isStrat) {
                if(!isExsit) {
                    if( ui2ServerForAppium == null) {
                        // 拉起uiautomator2
                        ui2ServerForAppium = new Ui2ServerForAppium(deviceId);
                        ui2ServerForAppium.start();
                        try {
                            TimeUnit.SECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if(ui2ServerForAppium != null) {
            ui2ServerForAppium.close();
        }
    }

    @Override
    public void close() {
        this.isRunning.set(false);
    }
}
