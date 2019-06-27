package com.testwa.distest.client.component.minicap;import lombok.extern.slf4j.Slf4j;import org.apache.commons.io.IOUtils;import java.io.Closeable;@Slf4jpublic class ScreenIOSProjection2 extends Thread implements Closeable {    private String deviceId;    private int port;    private IOSScreenServer2 server;    private ScreenListener listener;    public ScreenIOSProjection2(String deviceId, Integer port, ScreenListener listener) {        this.deviceId = deviceId;        this.port = port;        this.listener = listener;    }    private void startServer() {        this.server = new IOSScreenServer2(port);        this.server.start();    }    public boolean isRunning() {        if(this.server == null) {            return false;        }        return this.server.isRunning();    }    @Override    public void run() {        try {            startServer();            while (this.server.isRunning()) {                byte[] take1 = this.server.take();                if(take1 == null) {                    continue;                }                try {                    this.listener.projection(take1);                } catch (Exception e) {                    e.printStackTrace();                }            }        } catch (Exception e) {            log.error("屏幕映射失败", e);        } finally {            close();        }    }    @Override    public void close() {        IOUtils.closeQuietly(this.server);    }}