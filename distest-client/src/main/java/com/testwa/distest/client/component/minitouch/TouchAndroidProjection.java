package com.testwa.distest.client.component.minitouch;import lombok.extern.slf4j.Slf4j;import org.apache.commons.io.IOUtils;import java.io.IOException;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-07-13 10:46 **/@Slf4jpublic class TouchAndroidProjection {    private String deviceId;    private String resourcePath;    private Minitouch minitouch;    public TouchAndroidProjection(String deviceId, String resourcePath) {        this.deviceId = deviceId;        this.resourcePath = resourcePath;    }    /**     * 重启     */    public void restart() {        close();        start();    }    public void start() {        this.minitouch = new Minitouch(deviceId, resourcePath);        this.minitouch.start();    }    public boolean isRunning() {        if(this.minitouch == null) {            return false;        }        return this.minitouch.isRunning() && this.minitouch.isRunning();    }    /**     *@Description: 发送 minitouch 命令     *@Param: [shellCommand]     *@Return: void     *@Author: wen     *@Date: 2018/7/15     */    public void sendEvent(String command) {        try {            if(this.minitouch != null){                this.minitouch.sendEvent(command);            }        } catch (IOException e) {            log.error("minitouch 服务器出错，重启中......", e);            this.restart();        }    }    public void close() {        if(minitouch != null) {            minitouch.close();        }    }}