package com.testwa.distest.client.component.stfagent;import com.testwa.distest.client.android.ADBTools;import com.testwa.distest.client.util.PortUtil;import jp.co.cyberagent.stf.proto.Wire;import lombok.extern.slf4j.Slf4j;import org.apache.commons.io.IOUtils;import java.io.Closeable;import java.io.IOException;import java.io.OutputStream;import java.net.Socket;import java.util.concurrent.TimeUnit;import java.util.concurrent.atomic.AtomicBoolean;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-07-13 10:13 **/@Slf4jpublic class StfAgentClient extends Thread implements Closeable {    private static final String HOST = "127.0.0.1";    private static final String AB_AGENT_NAME = "stfagent";    private Integer agentPort;    private String deviceId;    private Socket socket;    private MessageWriter messageWriter = null;    private DevInformationAssembly devInformationAssembly = null;    private OutputStream outputStream = null;    /** 是否运行 */    private AtomicBoolean isRunning = new AtomicBoolean(false);    public StfAgentClient(String deviceId) {        super("stf-agent-client");        this.deviceId = deviceId;    }    /**     * 是否运行     * @return true 已运行 false 未运行     */    public boolean isRunning() {        return this.isRunning.get();    }    public DevInformationAssembly getDevInformationAssembly(){        return this.devInformationAssembly;    }    @Override    public void close() {        this.isRunning.set(false);        if(outputStream != null) {            try {                this.outputStream.close();            } catch (IOException e) {                e.printStackTrace();            }        }        IOUtils.closeQuietly(this.socket);        if(this.agentPort != null) {            ADBTools.forwardRemove(deviceId, this.agentPort);        }        this.interrupt();    }    @Override    public synchronized void start() {        if (this.isRunning.get()) {            throw new IllegalStateException("[" + deviceId + "] stf-agent-client已运行");        } else {            this.isRunning.set(true);        }        super.start();    }    @Override    public void run() {        // 连接stf-agent服务        try {            this.agentPort = PortUtil.getAvailablePort();            ADBTools.forward(deviceId, agentPort, AB_AGENT_NAME);            TimeUnit.SECONDS.sleep(1);            this.socket = new Socket(HOST, agentPort);            this.socket.setKeepAlive(true);        } catch (IOException | InterruptedException e) {            log.error("[{}] stf-agent-client 连接失败", deviceId, e);        }        // 开始运行stf-agent客户端        log.info("[{}] stf-agent-client 连接成功 {}", deviceId, agentPort);        try {            if(socket.isClosed()) {                this.socket = new Socket(HOST, agentPort);                this.socket.setKeepAlive(true);            }            outputStream = socket.getOutputStream();            messageWriter = new MessageWriter(outputStream);        } catch (Exception e) {            log.info("[{}] stf-agent-client 运行错误", deviceId, e);        }    }    public void onKeyEvent(int keyCode) {        Wire.KeyEventRequest.Builder builder = Wire.KeyEventRequest.newBuilder();        builder.setKeyCode(keyCode);        builder.setEvent(Wire.KeyEvent.DOWN);        Wire.KeyEventRequest request = builder.build();        Wire.Envelope.Builder envBuild = Wire.Envelope.newBuilder();        envBuild.setType(Wire.MessageType.DO_KEYEVENT);        envBuild.setMessage(request.toByteString());        Wire.Envelope envelope = envBuild.build();        executeKeyEvent(envelope);        Wire.KeyEventRequest.Builder builder2 = Wire.KeyEventRequest.newBuilder();        builder2.setKeyCode(keyCode);        builder2.setEvent(Wire.KeyEvent.UP);        Wire.Envelope.Builder envBuild2 = Wire.Envelope.newBuilder();        envBuild2.setType(Wire.MessageType.DO_KEYEVENT);        envBuild2.setMessage(builder2.build().toByteString());        executeKeyEvent(envBuild2.build());    }    public void onType(String text) {        Wire.DoTypeRequest.Builder request = Wire.DoTypeRequest.newBuilder();        request.setText(text);        Wire.Envelope.Builder builder = Wire.Envelope.newBuilder();        builder.setType(Wire.MessageType.DO_TYPE);        builder.setMessage(request.build().toByteString());        executeKeyEvent(builder.build());    }    public void setRotation(int rotation) {        Wire.SetRotationRequest.Builder request = Wire.SetRotationRequest.newBuilder();        request.setRotation(rotation);        Wire.Envelope.Builder builder = Wire.Envelope.newBuilder();        builder.setType(Wire.MessageType.SET_ROTATION);        builder.setMessage(request.build().toByteString());        executeKeyEvent(builder.build());    }    private void executeKeyEvent(Wire.Envelope input) {        if (messageWriter == null) {            return;        }        messageWriter.write(input);    }    /**     * 检查是否关闭     */    protected void checkClosed() {        if (!this.isRunning.get()) {            throw new IllegalStateException("[" + deviceId + "] stf-agent-client 已关闭");        }    }    public void getPackages() {        Wire.BrowserPackageEvent.Builder builder = Wire.BrowserPackageEvent.newBuilder();        builder.build();    }}