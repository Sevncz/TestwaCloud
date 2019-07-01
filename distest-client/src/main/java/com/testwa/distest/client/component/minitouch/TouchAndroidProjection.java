package com.testwa.distest.client.component.minitouch;import lombok.extern.slf4j.Slf4j;import org.apache.commons.io.IOUtils;import java.io.IOException;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-07-13 10:46 **/@Slf4jpublic class TouchAndroidProjection {    private String deviceId;    private String resourcePath;    private MinitouchServer server;    private MinitouchClient client;    public TouchAndroidProjection(String deviceId, String resourcePath) {        this.deviceId = deviceId;        this.resourcePath = resourcePath;    }    /**     * 重启     */    public void restart() {        this.server.restart();    }    public void start() {        this.server = new MinitouchServer(deviceId, resourcePath);        this.server.start();        int port = this.server.getPort();        this.client = new MinitouchClient(port, deviceId);        this.client.start();    }    public boolean isRunning() {        if(this.server == null) {            return false;        }        if(this.client == null) {            return false;        }        return this.server.isRunning() && this.client.isRunning();    }    /**     *@Description: 发送 minitouch 命令     *@Param: [shellCommand]     *@Return: void     *@Author: wen     *@Date: 2018/7/15     */    public void sendEvent(String command) {        try {            if(this.client != null){                this.client.sendEvent(command);            }        } catch (IOException e) {            log.error("minitouch 服务器出错，重启中......");            this.restart();        }    }    public void close() {        if(server != null) {            server.close();        }        if(client != null) {            IOUtils.closeQuietly(client);        }    }}