package com.testwa.distest.client.component.stfagent;import com.google.protobuf.InvalidProtocolBufferException;import com.testwa.distest.client.android.ADBTools;import com.testwa.distest.client.device.driver.AndroidRemoteControlDriver;import com.testwa.distest.client.util.PortUtil;import jp.co.cyberagent.stf.proto.Wire;import lombok.extern.slf4j.Slf4j;import org.apache.commons.io.IOUtils;import java.io.Closeable;import java.io.IOException;import java.io.InputStream;import java.io.OutputStream;import java.net.Socket;import java.util.concurrent.TimeUnit;import java.util.concurrent.atomic.AtomicBoolean;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-07-13 10:13 **/@Slf4jpublic class StfServiceClient extends Thread implements Closeable {    private static final String HOST = "127.0.0.1";    private static final String AB_SERVICE_NAME = "stfservice";    private Integer servicePort;    private String deviceId;    private Socket socket;    private MessageWriter messageWriter = null;    private DevInformationAssembly devInformationAssembly = null;    private AndroidRemoteControlDriver driver;    private OutputStream outputStream = null;    /** 是否运行 */    private AtomicBoolean isRunning = new AtomicBoolean(false);    public StfServiceClient(String deviceId, AndroidRemoteControlDriver driver) {        super("stf-service-client");        this.deviceId = deviceId;        this.driver = driver;    }    /**     * 是否运行     * @return true 已运行 false 未运行     */    public boolean isRunning() {        return this.isRunning.get();    }    public DevInformationAssembly getDevInformationAssembly(){        return this.devInformationAssembly;    }    @Override    public void close() {        this.isRunning.set(false);        IOUtils.closeQuietly(this.socket);        if(this.servicePort != null) {            ADBTools.forwardRemove(deviceId, this.servicePort);        }        this.interrupt();    }    @Override    public synchronized void start() {        if (this.isRunning.get()) {            throw new IllegalStateException("[" + deviceId + "] stf-service-client已运行");        } else {            this.isRunning.set(true);        }        super.start();    }    @Override    public void run() {        // 连接stf-service服务        try {            this.servicePort = PortUtil.getAvailablePort();            ADBTools.forward(deviceId, servicePort, AB_SERVICE_NAME);            TimeUnit.SECONDS.sleep(1);            this.socket = new Socket(HOST, servicePort);            this.socket.setKeepAlive(true);        } catch (IOException | InterruptedException e) {            log.error("[{}] stf-service-client 连接失败", deviceId, e);        }        // 开始运行stf-service客户端        log.info("[{}] stf-service-client 连接成功 {}", deviceId, servicePort);        try {            outputStream = socket.getOutputStream();            messageWriter = new MessageWriter(outputStream);            while (isRunning.get()) {                if(socket.isClosed()) {                    this.socket = new Socket(HOST, servicePort);                    this.socket.setKeepAlive(true);                }                InputStream inputStream = socket.getInputStream();                if(inputStream.available() <= 0) {                    try {                        TimeUnit.MILLISECONDS.sleep(1);                    }catch (InterruptedException e) {                    }                }                Wire.Envelope envelope = new MessageReader(inputStream).read();                if(envelope == null){                    try {                        TimeUnit.MILLISECONDS.sleep(1);                    }catch (InterruptedException e) {                    }                    continue;                }                if(devInformationAssembly == null) {                    buildDevInformationAssembly();                }                switch (envelope.getType()){                    case EVENT_BATTERY:                        handleEventBattery(envelope);                        break;                    case EVENT_CONNECTIVITY:                        handleEventConnect(envelope);                        break;                    case EVENT_ROTATION:                        handleEventRotation(envelope);                        break;                    case EVENT_AIRPLANE_MODE:                        handleEventAirplaneMode(envelope);                        break;                    case EVENT_BROWSER_PACKAGE:                        handleEventBrowserPackage(envelope);                        break;                    case EVENT_PHONE_STATE:                        handleEventPhoneMode(envelope);                        break;                    case DO_IDENTIFY:                        handleEventIdentify(envelope);                        break;                    case GET_CLIPBOARD:                        handleGetClipboard(envelope);                        break;                    case GET_PROPERTIES:                        handleGetProperties(envelope);                        break;                    case GET_VERSION:                        handleEventGetVision(envelope);                        break;                    case GET_DISPLAY:                        handleEventGetDisplay(envelope);                        break;                    case GET_WIFI_STATUS:                        handleEventGetWifiStatus(envelope);                        break;                    case GET_SD_STATUS:                        handleEventGetSdcardStatus(envelope);                        break;                    case GET_BROWSERS:                        handleGetBrowsers(envelope);                        break;                    case SET_CLIPBOARD:                        handleSetClipboard(envelope);                        break;                    case SET_KEYGUARD_STATE:                        handleSetKeyguardState(envelope);                        break;                    case SET_MASTER_MUTE:                        break;                    default:                        log.error("[{}] stf-service-client Unknowing eventType: {}", deviceId, envelope.getType());                }            }            log.debug("[{}] 连接中断", deviceId);        } catch (Exception e) {            log.info("[{}] stf-service-client 运行错误", deviceId, e);        } finally {            IOUtils.closeQuietly(this.socket);        }        this.isRunning.set(false);    }    private void handleEventBattery(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.BatteryEvent batteryEvent = Wire.BatteryEvent.parseFrom(envelope.getMessage());        // 每30秒打印一次//        log.info("[{}] battery health: {}, level: {}, scale: {}, status: {}", deviceId, batteryEvent.getHealth(), batteryEvent.getLevel(), batteryEvent.getScale(), batteryEvent.getStatus());    }    private void handleEventConnect(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.ConnectivityEvent connectivityEvent = Wire.ConnectivityEvent.parseFrom(envelope.getMessage());        log.info("[{}] connectivity {}", deviceId, connectivityEvent.getConnected());    }    private void handleEventRotation(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.RotationEvent rotationEvent = Wire.RotationEvent.parseFrom(envelope.getMessage());        int rotation = rotationEvent.getRotation();        this.driver.changeRotation(rotation);    }    private void handleEventBrowserPackage(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.BrowserPackageEvent browserPackageEvent = Wire.BrowserPackageEvent.parseFrom(envelope.getMessage());        browserPackageEvent.getAppsList().forEach(app -> {            log.info("[{}] app info {}", deviceId, app.getName());        });    }    private void handleEventAirplaneMode(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.AirplaneModeEvent airplaneModeEvent = Wire.AirplaneModeEvent.parseFrom(envelope.getMessage());    }    private void handleEventPhoneMode(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.PhoneStateEvent phoneStateEvent = Wire.PhoneStateEvent.parseFrom(envelope.getMessage());        log.info("[{}] phone state {}", deviceId, phoneStateEvent.getState());    }    private void handleGetClipboard(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.GetClipboardResponse clipboardResponse = Wire.GetClipboardResponse.parseFrom(envelope.getMessage());    }    private void handleGetProperties(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.GetPropertiesResponse propertiesResponse = Wire.GetPropertiesResponse.parseFrom(envelope.getMessage());        for (Wire.Property property : propertiesResponse.getPropertiesList()){            getDevInformationAssembly().getDevProperty().getDevPropertiesHashMap().put(property.getName(),property.getValue());        }    }    private void handleGetBrowsers(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.GetBrowsersResponse browserPackageEvent = Wire.GetBrowsersResponse.parseFrom(envelope.getMessage());//        browserPackageEvent.getAppsList().forEach(app -> {//            log.info("[{}] app info {}", deviceId, app.getName());//        });    }    private void handleEventIdentify(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.DoIdentifyResponse identifyResponse = Wire.DoIdentifyResponse.parseFrom(envelope.getMessage());        getDevInformationAssembly().getDevDoIdentify().setDevIdentify(identifyResponse.getSuccess());    }    private void handleEventGetVision(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.GetVersionResponse visionResponse = Wire.GetVersionResponse.parseFrom(envelope.getMessage());        getDevInformationAssembly().getDevVision().setDevSerial(visionResponse.getVersion());    }    private void handleEventGetWifiStatus(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.GetWifiStatusResponse wifiStatusResponse = Wire.GetWifiStatusResponse.parseFrom(envelope.getMessage());        getDevInformationAssembly().getDevWifiStatus().setWifiStatus(wifiStatusResponse.getStatus());    }    private void handleEventGetSdcardStatus(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.GetSdStatusResponse sdStatusResponse = Wire.GetSdStatusResponse.parseFrom(envelope.getMessage());        getDevInformationAssembly().getDevSdcardStatus().setMounted(sdStatusResponse.getMounted());    }    private void handleEventGetDisplay(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.GetDisplayResponse displayResponse = Wire.GetDisplayResponse.parseFrom(envelope.getMessage());        getDevInformationAssembly().getDevDisplay().setScreenDensity(displayResponse.getDensity());        getDevInformationAssembly().getDevDisplay().setScreenFps(displayResponse.getFps());        getDevInformationAssembly().getDevDisplay().setScreenHeight(displayResponse.getHeight());        getDevInformationAssembly().getDevDisplay().setScreenWidth(displayResponse.getWidth());        getDevInformationAssembly().getDevDisplay().setScreenXdpi(displayResponse.getXdpi());        getDevInformationAssembly().getDevDisplay().setScreenYdpi(displayResponse.getYdpi());        getDevInformationAssembly().getDevDisplay().setScreenRotation(displayResponse.getRotation());    }    private void handleSetClipboard(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.SetClipboardResponse clipboardResponse = Wire.SetClipboardResponse.parseFrom(envelope.getMessage());    }    private void handleSetKeyguardState(Wire.Envelope envelope) throws InvalidProtocolBufferException {        Wire.SetKeyguardStateRequest setKeyguardStateEvent = Wire.SetKeyguardStateRequest.parseFrom(envelope.getMessage());    }    /**     * 检查是否关闭     */    protected void checkClosed() {        if (!this.isRunning.get()) {            throw new IllegalStateException("[" + deviceId + "] stf-service-client 已关闭");        }    }    public void getPackages() {        Wire.BrowserPackageEvent.Builder builder = Wire.BrowserPackageEvent.newBuilder();        builder.build();    }    public void executeKeyEvent(Wire.Envelope input) {        if (messageWriter == null) {            return;        }        messageWriter.write(input);    }    public void buildDevInformationAssembly() {        if(devInformationAssembly == null) {            this.devInformationAssembly = new DevInformationAssembly(deviceId);        }        executeKeyEvent(devInformationAssembly.getDevDoIdentify().getDoIdentifyEnvelope());        executeKeyEvent(devInformationAssembly.getDevDisplay().getDisplayRequestEnvelope());        executeKeyEvent(devInformationAssembly.getDevProperty().getPropertiesEnvelope());        executeKeyEvent(devInformationAssembly.getDevWifiStatus().getWifiStatusEnvelope());//        executeKeyEvent(devInformationAssembly.getDevVision().getDevVisionEnvelope());//        executeKeyEvent(devInformationAssembly.getDevSdcardStatus().getSdcardStatusEnvelope());    }}