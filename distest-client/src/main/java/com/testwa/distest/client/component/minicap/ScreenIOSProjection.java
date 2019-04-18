package com.testwa.distest.client.component.minicap;import lombok.extern.slf4j.Slf4j;import org.apache.commons.io.IOUtils;import java.io.Closeable;import java.util.concurrent.TimeoutException;@Slf4jpublic class ScreenIOSProjection extends Thread implements Closeable {    /** 分辨率 *///    private String resolution = "400x600";//    private String resolution = "750x1334";    private String resolution = "375x667";    private String deviceId;    private MinicapIOSServer server;    private MinicapClient client;    private ScreenListener listener;    public ScreenIOSProjection(String deviceId, ScreenListener listener) {        this.deviceId = deviceId;        this.listener = listener;    }    /**     * 设置分辨率     * @param resolution 缩放比例     */    public void setResolution(String resolution) {        this.resolution = resolution;    }    /**     * 重启     */    public void restart() {        this.server.setResolution(resolution);        this.server.restart();    }    private void startServer() throws TimeoutException, InterruptedException {        this.server = new MinicapIOSServer(deviceId);        this.server.setResolution(resolution);        this.server.start();    }    private void startClient() {        int port = this.server.getPort();        this.client = new MinicapClient(port);        this.client.start();    }    public boolean isRunning() {        if(this.server == null) {            return false;        }        if(this.client == null) {            return false;        }        return this.server.isRunning() && this.client.isRunning();    }    @Override    public void run() {        try {            startServer();            startClient();            while (this.server.isRunning()) {                byte[] take1 = this.client.take();                try {                    this.listener.projection(take1);                } catch (Exception e) {                    e.printStackTrace();                }                long start = System.currentTimeMillis();                byte[] take = null;                while(true) {                    long end = System.currentTimeMillis();                    if((end - start) >= 150 && take != null) {                        this.listener.projection(take);                        break;                    }                    take = this.client.take();                }            }        } catch (Exception e) {            log.error("屏幕映射失败", e);        } finally {            close();        }    }    @Override    public void close() {        IOUtils.closeQuietly(server);        IOUtils.closeQuietly(client);    }}