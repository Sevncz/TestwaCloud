package com.testwa.distest.client.component.minicap;

import com.testwa.distest.client.component.wda.driver.IOSDriver;
import org.apache.commons.io.IOUtils;

public class ScreenIOSLowerProjection {

    private String udid;
    private String resourcePath;
    private IOSDriver driver;
    private IOSScreenLowerServer server;
    private ScreenProjectionObserver screenProjectionObserver;

    public ScreenIOSLowerProjection(String udid, String resourcePath, ScreenProjectionObserver screenProjectionObserver) {
        this.udid = udid;
        this.resourcePath = resourcePath;
        this.screenProjectionObserver = screenProjectionObserver;
    }

    public void startServer() {
        this.server = new IOSScreenLowerServer(udid, resourcePath);
        this.server.registerObserver(screenProjectionObserver);
        this.server.start();
    }

    public boolean isRunning() {
        if(this.server == null) {
            return false;
        }
        return this.server.isRunning();
    }

    public void close() {
        this.server.close();
//        IOUtils.closeQuietly(this.server);
    }
}
