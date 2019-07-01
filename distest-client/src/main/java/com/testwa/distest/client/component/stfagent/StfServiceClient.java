package com.testwa.distest.client.component.stfagent;import com.google.protobuf.InvalidProtocolBufferException;import com.testwa.distest.client.device.driver.AndroidRemoteControlDriver;import jp.co.cyberagent.stf.proto.Wire;import lombok.extern.slf4j.Slf4j;import org.apache.commons.io.IOUtils;import java.io.Closeable;import java.io.IOException;import java.io.InputStream;import java.io.OutputStream;import java.net.Socket;import java.util.concurrent.TimeUnit;import java.util.concurrent.atomic.AtomicBoolean;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-07-13 10:13 **/@Slf4jpublic class StfServiceClient extends Thread implements Closeable {    private static final String HOST = "127.0.0.1";    private Integer servicePort;    private String deviceId;    private Socket socket;    private MessageWriter messageWriter = null;    private DevInformationAssembly devInformationAssembly = null;    private AndroidRemoteControlDriver driver;    /** 是否运行 */    private AtomicBoolean isRunning = new AtomicBoolean(false);    public StfServiceClient(int agentPort, String deviceId, AndroidRemoteControlDriver driver) {        super("stf-service-client");        this.servicePort = agentPort;        this.deviceId = deviceId;        this.driver = driver;    }    /**     * 是否运行     * @return true 已运行 false 未运行     */    public boolean isRunning() {        return this.isRunning.get();    }    public DevInformationAssembly getDevInformationAssembly(){        return this.devInformationAssembly;    }    @Override    public void close() {        this.isRunning.set(false);        IOUtils.closeQuietly(this.socket);        this.interrupt();    }    @Override    public synchronized void start() {        if (this.isRunning.get()) {            throw new IllegalStateException("[" + deviceId + "] stf-service-client已运行");        } else {            this.isRunning.set(true);        }        super.start();    }    @Override    public void run() {        // 连接stf-agent服务        try {            TimeUnit.SECONDS.sleep(1);            this.socket = new Socket(HOST, servicePort);            this.socket.setKeepAlive(true);        } catch (IOException | InterruptedException e) {            log.error("[{}] stf-service-client 连接失败", deviceId, e);        }        // 开始运行stf-service客户端        log.info("[{}] stf-service-client 连接成功 {}", deviceId, servicePort);        try {            while (isRunning.get()) {                if(socket.isClosed()) {                    this.socket = new Socket(HOST, servicePort);                    this.socket.setKeepAlive(true);                }                InputStream inputStream = socket.getInputStream();                if(inputStream.available() <= 0) {                    try {                        TimeUnit.MILLISECONDS.sleep(1);                    }catch (InterruptedException e) {                    }                }                Wire.Envelope envelope = new MessageReader(inputStream).read();                if(envelope == null){                    try {                        TimeUnit.MILLISECONDS.sleep(1);                    }catch (InterruptedException e) {                    }                    continue;                }                switch (envelope.getType()){                    case EVENT_BATTERY:                        handleEventBattery(envelope);                        break;                    case EVENT_CONNECTIVITY:                        handleEventConnect(envelope);                        break;                    case EVENT_ROTATION:                        handleEventRotation(envelope);                        break;                    case EVENT_AIRPLANE_MODE:                        handleEventAirplaneMode(envelope);                        break;                    case EVENT_BROWSER_PACKAGE:                        handleEventBrowserPackage(envelope);                        break;                    case EVENT_PHONE_STATE:                        handleEventPhoneMode(envelope);                        break;                    case DO_IDENTIFY:                        handleEventIdentify(envelope);                        break;                    case GET_CLIPBOARD:                        handleGetClipboard(envelope);                        break;                    case GET_PROPERTIES:                        handleGetProperties(envelope);                        break;                    case GET_VERSION:                        handleEventGetVision(envelope);                        break;                    case GET_DISPLAY:                        handleEventGetDisplay(envelope);                        break;                    case GET_WIFI_STATUS:                        handleEventGetWifiStatus(envelope);                        break;                    case GET_SD_STATUS:                        handleEventGetSdcardStatus(envelope);                        break;                    default:                        log.error("[{}] stf-service-client Unknowing eventType: {}", deviceId, envelope.getType());                }            }            log.debug("[{}] 连接中断", deviceId);        } catch (Exception e) {            log.info("[{}] stf-service-client 运行错误", deviceId, e);        } finally {            IOUtils.closeQuietly(this.socket);        }        this.isRunning.set(false);    }    private void handleEventBattery(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.BatteryEvent batteryEvent = Wire.BatteryEvent.parseFrom(envelope.getMessage());    }    private void handleEventConnect(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.ConnectivityEvent connectivityEvent = Wire.ConnectivityEvent.parseFrom(envelope.getMessage());    }    private void handleEventRotation(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.RotationEvent rotationEvent = Wire.RotationEvent.parseFrom(envelope.getMessage());        int rotation = rotationEvent.getRotation();        this.driver.changeRotation(rotation);    }    private void handleEventBrowserPackage(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.RotationEvent rotationEvent = Wire.RotationEvent.parseFrom(envelope.getMessage());    }    private void handleEventAirplaneMode(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.AirplaneModeEvent airplaneModeEvent = Wire.AirplaneModeEvent.parseFrom(envelope.getMessage());    }    private void handleEventPhoneMode(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.PhoneStateEvent phoneStateEvent = Wire.PhoneStateEvent.parseFrom(envelope.getMessage());    }    private void handleGetClipboard(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.GetClipboardResponse clipboardResponse = Wire.GetClipboardResponse.parseFrom(envelope.getMessage());    }    private void handleGetProperties(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.GetPropertiesResponse propertiesResponse = Wire.GetPropertiesResponse.parseFrom(envelope.getMessage());        for (Wire.Property property : propertiesResponse.getPropertiesList()){            getDevInformationAssembly().getDevProperty().getDevPropertiesHashMap().put(property.getName(),property.getValue());        }    }    private void handleEventIdentify(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.DoIdentifyResponse identifyResponse = Wire.DoIdentifyResponse.parseFrom(envelope.getMessage());        getDevInformationAssembly().getDevDoIdentify().setDevIdentify(identifyResponse.getSuccess());    }    private void handleEventGetVision(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.GetVersionResponse visionResponse = Wire.GetVersionResponse.parseFrom(envelope.getMessage());        getDevInformationAssembly().getDevVision().setDevSerial(visionResponse.getVersion());    }    private void handleEventGetWifiStatus(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.GetWifiStatusResponse wifiStatusResponse = Wire.GetWifiStatusResponse.parseFrom(envelope.getMessage());        getDevInformationAssembly().getDevWifiStatus().setWifiStatus(wifiStatusResponse.getStatus());    }    private void handleEventGetSdcardStatus(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.GetSdStatusResponse sdStatusResponse = Wire.GetSdStatusResponse.parseFrom(envelope.getMessage());        getDevInformationAssembly().getDevSdcardStatus().setMounted(sdStatusResponse.getMounted());    }    private void handleEventGetDisplay(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.GetDisplayResponse displayResponse = Wire.GetDisplayResponse.parseFrom(envelope.getMessage());        getDevInformationAssembly().getDevDisplay().setScreenDensity(displayResponse.getDensity());        getDevInformationAssembly().getDevDisplay().setScreenFps(displayResponse.getFps());        getDevInformationAssembly().getDevDisplay().setScreenHeight(displayResponse.getHeight());        getDevInformationAssembly().getDevDisplay().setScreenWidth(displayResponse.getWidth());        getDevInformationAssembly().getDevDisplay().setScreenXdpi(displayResponse.getXdpi());        getDevInformationAssembly().getDevDisplay().setScreenYdpi(displayResponse.getYdpi());        getDevInformationAssembly().getDevDisplay().setScreenRotation(displayResponse.getRotation());    }    /**     * 检查是否关闭     */    protected void checkClosed() {        if (!this.isRunning.get()) {            throw new IllegalStateException("[" + deviceId + "] stf-service-client 已关闭");        }    }    public void getPackages() {        Wire.BrowserPackageEvent.Builder builder = Wire.BrowserPackageEvent.newBuilder();        builder.build();    }}