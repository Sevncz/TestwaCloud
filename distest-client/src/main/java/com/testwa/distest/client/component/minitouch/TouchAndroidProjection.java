package com.testwa.distest.client.component.minitouch;import com.testwa.distest.client.component.stfagent.StfAgentClient;import com.testwa.distest.client.component.stfagent.StfAgentServer;import lombok.extern.slf4j.Slf4j;import org.apache.commons.io.IOUtils;import java.io.Closeable;import java.io.IOException;import java.sql.Time;import java.util.concurrent.TimeUnit;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-07-13 10:46 **/@Slf4jpublic class TouchAndroidProjection extends Thread implements Closeable {    private String deviceId;    private String resourcePath;    private MinitouchServer server;    private MinitouchClient client;    private StfAgentServer stfAgentServer;    private StfAgentClient stfAgentClient;    public TouchAndroidProjection(String deviceId, String resourcePath) {        this.deviceId = deviceId;        this.resourcePath = resourcePath;    }    /**     * 重启     */    public void restart() {        this.server.restart();        this.stfAgentServer.restart();    }    private void startServer() {        this.server = new MinitouchServer(deviceId, resourcePath);        this.server.start();    }    private void startClient() {        int port = this.server.getPort();        this.client = new MinitouchClient(port, deviceId);        while (!this.server.isRunning()){            try {                TimeUnit.SECONDS.sleep(1);            } catch (InterruptedException e) {            }        }        this.client.start();    }    private void startStfAgentServer() {        this.stfAgentServer = new StfAgentServer(deviceId, resourcePath);        this.stfAgentServer.start();    }    private void startStfAgentClient() {        int port = this.stfAgentServer.getAgentPort();        while (!this.stfAgentServer.isRunning()){            try {                TimeUnit.SECONDS.sleep(1);            } catch (InterruptedException e) {            }        }        this.stfAgentClient = new StfAgentClient(port);        this.stfAgentClient.start();    }    public boolean isRunning() {        if(this.server == null) {            return false;        }        if(this.client == null) {            return false;        }        return this.server.isRunning() && this.client.isRunning();    }    /**     *@Description: 发送 minitouch 命令     *@Param: [shellCommand]     *@Return: void     *@Author: wen     *@Date: 2018/7/15     */    public void sendEvent(String command) {        try {            if(this.client != null){                this.client.sendEvent(command);            }        } catch (IOException e) {            log.error("minitouch 服务器出错，重启中......");            this.restart();        }    }    /**     *@Description: 发送 stf key code     *@Param: [code]     *@Return: void     *@Author: wen     *@Date: 2018/7/15     */    public void sendCode(int code) {        if(this.stfAgentClient != null){            this.stfAgentClient.onKeyEvent(code);        }    }    public void sendText(String text) {        if(this.stfAgentClient != null){            this.stfAgentClient.onType(text);        }    }    public void getInstalledBrowsers() {        if(this.stfAgentClient != null){            this.stfAgentClient.getPackages();        }    }    @Override    public void run() {        try {            startServer();            startStfAgentServer();            TimeUnit.SECONDS.sleep(5);            startClient();            startStfAgentClient();            while (this.server.isRunning()) {                TimeUnit.SECONDS.sleep(1);            }        } catch (Exception e) {            log.error("Touch映射失败", e);        } finally {            close();        }    }    @Override    public void close() {        if(server != null) {            server.close();        }        if(client != null) {            IOUtils.closeQuietly(client);        }        if(stfAgentClient != null) {            stfAgentServer.close();        }        if(stfAgentClient != null) {            IOUtils.closeQuietly(stfAgentClient);        }    }}