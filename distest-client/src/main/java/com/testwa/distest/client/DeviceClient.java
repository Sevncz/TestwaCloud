package com.testwa.distest.client;import com.alibaba.fastjson.JSON;import com.alibaba.fastjson.JSONObject;import com.android.ddmlib.IDevice;import com.android.ddmlib.Log;import com.google.protobuf.ByteString;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.distest.client.android.ADBCommandUtils;import com.testwa.distest.client.android.AndroidHelper;import com.testwa.distest.client.android.PhysicalSize;import com.testwa.distest.client.component.Constant;import com.testwa.distest.client.component.appium.utils.Config;import com.testwa.distest.client.component.executor.*;import com.testwa.distest.client.component.logcat.DLogger;import com.testwa.distest.client.component.logcat.LogCatFilter;import com.testwa.distest.client.component.logcat.LogCatMessage;import com.testwa.distest.client.component.logcat.LogListener;import com.testwa.distest.client.component.minicap.*;import com.testwa.distest.client.callback.DefaultObserver;import com.testwa.distest.client.component.minitouch.TouchProjection;import com.testwa.distest.client.exception.DeviceNotReadyException;import com.testwa.distest.client.ios.IOSDeviceUtil;import com.testwa.distest.client.service.Gvice;import io.grpc.Channel;import io.rpc.testwa.device.DeviceType;import io.rpc.testwa.device.LogcatMessageRequest;import io.rpc.testwa.device.LogcatRequest;import io.rpc.testwa.device.ScreenCaptureRequest;import io.rpc.testwa.push.ClientInfo;import io.rpc.testwa.push.PushGrpc;import io.rpc.testwa.push.Status;import io.rpc.testwa.push.TopicInfo;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import java.io.File;import java.io.IOException;import java.nio.charset.StandardCharsets;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.ArrayList;import java.util.List;import java.util.Map;import java.util.concurrent.*;import java.util.concurrent.atomic.AtomicReference;import java.util.regex.Matcher;import java.util.regex.Pattern;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-08 16:27 **/@Slf4jpublic class DeviceClient implements ScreenListener, LogListener, TestTaskListener {    private final PushGrpc.PushStub asyncStub;    private final PushGrpc.PushBlockingStub blockingStub;    private final Channel channel;    private final String clientId;    private final String userToken;    private final ClientInfo clientInfo;    private boolean isWaitting = false;    private boolean isLogcatWaitting = false;    private DeviceType deviceType;    private ScreenAndroidProjection screenAndroidProjection = null;    private ScreenIOSProjection screenIOSProjection = null;    private TouchProjection touchProjection = null;    private DLogger dLogger = null;    private AbstractTestTask task = null;    private int buildClientInfoMaxTime = 10;    private final AtomicReference<Thread> currentTaskThread;    private final AtomicReference<ExecutorService> es;    private String videoOutputFile;    //  在Android的ADB的情况下，我们是使用adb logcat -v brief -v threadtime    private final static String adb_log_line_regex = "(.\\S*) *(.\\S*) *(\\d*) *(\\d*) *([A-Z]) *([^:]*): *(.*?)$";    private LogCatFilter logCatFilter;    private Pattern logcatPattern;    private String resourcePath;    private static final String PATH_SERVICE_APK = "STFService" + File.separator + "STFService.apk";    private static final String PATH_APPIUM_UIAUTOMATOR_SERVER = "appium" + File.separator + "appium-uiautomator2-server.apk";    private static final String PATH_APPIUM_UIAUTOMATOR_DEBUG = "appium" + File.separator + "appium-uiautomator2-server-debug-androidTest.apk";    private static final String PATH_KEYBOARDSERVICE = "keyboardservice" + File.separator + "keyboardservice-debug.apk";    // appium 相关包    private static final String UNICODEIME_PACKAGE = "io.appium.android.ime";    private static final String UI2_SERVER_PACKAGE = "io.appium.uiautomator2.server";    private static final String UI2_DEBUG_PACKAGE = "io.appium.uiautomator2.server.test";    private static final String SELENDROID_SERVER_PACKAGE = "io.selendroid.server";    private static final String SETTINGS_PACKAGE = "io.appium.settings.Settings";    private static final String UNLOCK_PACKAGE = "io.appium.unlock";    // stf 相关包    private static final String STF_SERVICE_PACKAGE = "jp.co.cyberagent.stf";    // keyboardservice 相关包    private static final String KEYBOARD_SERVICE_PACKAGE = "com.android.adbkeyboard";    public DeviceClient(String deviceId, Channel channel, String userToken, DeviceType deviceType) throws DeviceNotReadyException {        this.resourcePath = Config.getString("distest.agent.resources");        this.deviceType = deviceType;        this.clientId = deviceId;        this.userToken = userToken;        this.channel = channel;        this.asyncStub = PushGrpc.newStub(channel);        this.blockingStub = PushGrpc.newBlockingStub(channel);        this.clientInfo = buildClientInfo();        ExecutorService taskWorker = Executors.newSingleThreadExecutor();        es = new AtomicReference<>();        es.set(taskWorker);        currentTaskThread = new AtomicReference<>();        logcatPattern = Pattern.compile(adb_log_line_regex);        log.info("设备 {} {} 初始化 ", clientInfo.getBrand(), clientInfo.getModel());    }    /**     * grpc     */    public void resendRegister(DefaultObserver defaultObserver) {        this.asyncStub.registerToServer(this.clientInfo, defaultObserver);    }    public void registerToServer(){        this.asyncStub.registerToServer(this.clientInfo, new DefaultObserver(this.clientId, this));    }    public String subscribe(String topic){        TopicInfo topicInfo = TopicInfo.newBuilder().setTopicName(topic).setClientInfo(this.clientInfo).build();        Status status = this.blockingStub.subscribe(topicInfo);        return status.getStatus();    }    public String cancel(String topic){        TopicInfo topicInfo = TopicInfo.newBuilder().setTopicName(topic).setClientInfo(this.clientInfo).build();        Status status = this.blockingStub.cancel(topicInfo);        return status.getStatus();    }    public String logoutFromServer(){        Status status = this.blockingStub.logoutFromServer(this.clientInfo);        return status.getStatus();    }    public String getClientId() {        return clientId;    }    public ClientInfo buildClientInfo() throws DeviceNotReadyException {        if(DeviceType.ANDROID.equals(this.deviceType)) {            IDevice dev = AndroidHelper.getInstance().getAndroidDevice(this.clientId).getDevice();            if(dev != null && dev.isOnline()){                String brand = dev.getProperty("ro.product.brand");                if(StringUtils.isBlank(brand)){                    if(buildClientInfoMaxTime <= 0){                        throw new DeviceNotReadyException("无法获取设备属性");                    }                    buildClientInfoMaxTime--;                    try {                        TimeUnit.SECONDS.sleep(1);                    } catch (InterruptedException e) {                        e.printStackTrace();                    }                    return buildClientInfo();                }                String cpuabi = dev.getProperty(IDevice.PROP_DEVICE_CPU_ABI);                String sdk = dev.getProperty(IDevice.PROP_BUILD_API_LEVEL);                String host = dev.getProperty("ro.build.host");                String model = dev.getProperty(IDevice.PROP_DEVICE_MODEL);                String version = dev.getProperty(IDevice.PROP_BUILD_VERSION);                String density = dev.getDensity() + "";                PhysicalSize size = ADBCommandUtils.getPhysicalSize(dev.getSerialNumber());                String width = String.valueOf(size.getWidth());                String height = String.valueOf(size.getHeight());                // 检查 uiautomator2、stfagent 等组件的安装情况//                String stfagent = this.resourcePath + File.separator + PATH_SERVICE_APK;                boolean stfagentInstall = ADBCommandUtils.isInstalledBasepackage(dev.getSerialNumber(), STF_SERVICE_PACKAGE);                if(!stfagentInstall) {                    log.warn("{} 未安装 {}", dev.getName(), STF_SERVICE_PACKAGE);                }//                String appiumServer = this.resourcePath + File.separator + PATH_APPIUM_UIAUTOMATOR_SERVER;                boolean appiumServerInstall = ADBCommandUtils.isInstalledBasepackage(dev.getSerialNumber(), UI2_SERVER_PACKAGE);                if(!appiumServerInstall) {                    log.warn("{} 未安装 {}", dev.getName(), UI2_SERVER_PACKAGE);                }//                String appiumDebug = this.resourcePath + File.separator + PATH_APPIUM_UIAUTOMATOR_DEBUG;                boolean appiumDebugInstall = ADBCommandUtils.isInstalledBasepackage(dev.getSerialNumber(), UI2_DEBUG_PACKAGE);                if(!appiumDebugInstall) {                    log.warn("{} 未安装 {}", dev.getName(), UI2_DEBUG_PACKAGE);                }                boolean settingsInstall = ADBCommandUtils.isInstalledBasepackage(dev.getSerialNumber(), SETTINGS_PACKAGE);                if(!settingsInstall) {                    log.warn("{} 未安装 {}", dev.getName(), SETTINGS_PACKAGE);                }                boolean unlockInstall = ADBCommandUtils.isInstalledBasepackage(dev.getSerialNumber(), UNLOCK_PACKAGE);                if(!unlockInstall) {                    log.warn("{} 未安装 {}", dev.getName(), UNLOCK_PACKAGE);                }                boolean unicodeIMEInstall = ADBCommandUtils.isInstalledBasepackage(dev.getSerialNumber(), UNICODEIME_PACKAGE);                if(!unicodeIMEInstall) {                    log.warn("{} 未安装 {}", dev.getName(), UNICODEIME_PACKAGE);                }                boolean selendroidInstall = ADBCommandUtils.isInstalledBasepackage(dev.getSerialNumber(), SELENDROID_SERVER_PACKAGE);                if(!selendroidInstall) {                    log.warn("{} 未安装 {}", dev.getName(), SELENDROID_SERVER_PACKAGE);                }//                String keyboardservice = this.resourcePath + File.separator + PATH_KEYBOARDSERVICE;                boolean keyboardserviceInstall = ADBCommandUtils.isInstalledBasepackage(dev.getSerialNumber(), KEYBOARD_SERVICE_PACKAGE);                if(!keyboardserviceInstall) {                    log.warn("{} 未安装 {}", dev.getName(), KEYBOARD_SERVICE_PACKAGE);                }                return ClientInfo.newBuilder()                        .setDeviceId(dev.getSerialNumber())                        .setBrand(brand)                        .setCpuabi(cpuabi)                        .setDensity(density)                        .setHeight(height)                        .setWidth(width)                        .setHost(host)                        .setModel(model)                        .setOsName("Android")                        .setSdk(sdk)                        .setUserFlag(userToken)                        .setVersion(version)                        .setSftagentInstall(stfagentInstall)                        .setAppiumUiautomator2ServerInstall(appiumServerInstall)                        .setAppiumUiautomator2DebugInstall(appiumDebugInstall)                        .setKeyboardserviceInstall(keyboardserviceInstall)                        .setSelendroidInstall(selendroidInstall)                        .setSettingsInstall(settingsInstall)                        .setUnicodeIMEInstall(unicodeIMEInstall)                        .setUnlockInstall(unlockInstall)                        .build();            }else{                throw new DeviceNotReadyException("设备不在线，无法初始化");            }        }else{            String cpu = IOSDeviceUtil.getCPUArchitecture(clientId);            String productType = IOSDeviceUtil.getProductType(clientId);            String productVersion = IOSDeviceUtil.getProductVersion(clientId);            return ClientInfo.newBuilder()                    .setDeviceId(clientId)                    .setBrand("Apple")                    .setCpuabi(cpu)                    .setModel(productType)                    .setOsName("iOS")                    .setUserFlag(userToken)                    .setVersion(productVersion)                    .build();        }    }    public void stop() {        // 关闭组件        stopMinicap();        stopMinitouch();        if (task != null) {            task.terminate();        }        this.setWaitting(true);        this.es.get().shutdown();    }    @Override    public void projection(byte[] data) {        if (!isWaitting) {            sendImage(data);        }    }    public void setWaitting(boolean waitting) {        log.info("set waitting = {}", waitting);        isWaitting = waitting;    }    private void sendImage(byte[] data) {        try {            ScreenCaptureRequest request = ScreenCaptureRequest.newBuilder()                    .setImg(ByteString.copyFrom(data))                    .setName("xxx")                    .setSerial(this.clientId)                    .build();            Gvice.deviceService(this.channel).screen(request);        }catch (Exception e){            log.error(e.getMessage());        }finally {        }    }    public void startMinicap(String command) {        this.setWaitting(false);        long startTime = System.currentTimeMillis();        log.warn("{} ready to start minicap, time: {}", clientInfo.getModel(), startTime);        // 获取请求的配置        JSONObject obj = JSON.parseObject(command);        Float scale = obj.getFloat("scale");        Integer rotate = obj.getInteger("rotate");//        scale = 0.2f;        if (scale == null) {scale = 0.3f;}        if (scale < 0.01) {scale = 0.01f;}        if (scale > 1.0) {scale = 1.0f;}        if (rotate == null) { rotate = 0; }        int quality = 25;        String resolution = obj.getString("resolution");        if(DeviceType.ANDROID.equals(this.deviceType)) {            if(this.screenAndroidProjection != null && this.screenAndroidProjection.isAlive()){                this.screenAndroidProjection.setZoom(scale);                this.screenAndroidProjection.setRotate(rotate);                this.screenAndroidProjection.setQuality(quality);                if(this.screenAndroidProjection.isRunning()) {                    this.screenAndroidProjection.restart();                }else{                    screenAndroidProjectionStart(scale, rotate, quality);                }            }else{                screenAndroidProjectionStart(scale, rotate, quality);            }        }else{            if(StringUtils.isBlank(resolution)) {                resolution = "400x600";            }            if(this.screenIOSProjection != null && this.screenIOSProjection.isAlive()){                this.screenIOSProjection.setResolution(resolution);                if(this.screenIOSProjection.isRunning()) {                    this.screenIOSProjection.restart();                }else{                    screenIOSProjectionStart(resolution);                }            }else{                screenIOSProjectionStart(resolution);            }        }        Long endTime = System.currentTimeMillis();        try {            TimeUnit.SECONDS.sleep(2);        } catch (InterruptedException e) {        }    }    private void screenAndroidProjectionStart(Float scale, Integer rotate, int quality) {        if(this.screenAndroidProjection != null) {            this.screenAndroidProjection.close();        }        this.screenAndroidProjection = new ScreenAndroidProjection(clientId, this);        this.screenAndroidProjection.setZoom(scale);        this.screenAndroidProjection.setRotate(rotate);        this.screenAndroidProjection.setQuality(quality);        this.screenAndroidProjection.start();    }    private void defaultVideoRecorderStart(Long taskCode) {        // 录制视频输出文件        Path videoPath = Paths.get(Constant.localVideoPath, this.clientId, taskCode + ".mp4");        if(!Files.exists(videoPath)) {            try {                Files.createDirectories(videoPath.getParent());//                Files.createFile(videoPath);            } catch (IOException e) {            }        }        this.videoOutputFile = videoPath.toString();        log.info("视频输出至：{}", videoOutputFile);        videoRecorderStart(0.35f, 0, 80, this.videoOutputFile);    }    private void videoRecorderStart(Float scale, Integer rotate, int quality, String outputFile) {        if(this.screenAndroidProjection != null) {            this.screenAndroidProjection.close();        }        this.screenAndroidProjection = new ScreenAndroidProjection(clientId, this);        this.screenAndroidProjection.setZoom(scale);        this.screenAndroidProjection.setRotate(rotate);        this.screenAndroidProjection.setQuality(quality);        this.screenAndroidProjection.initRecorder(outputFile);        this.screenAndroidProjection.start();    }    private void screenIOSProjectionStart(String resolution) {        this.screenIOSProjection = new ScreenIOSProjection(clientId, this);        this.screenIOSProjection.setResolution(resolution);        this.screenIOSProjection.start();    }    public void startMinitouch() {        if(DeviceType.ANDROID.equals(this.deviceType)) {            if (touchProjection != null) {                touchProjection.close();            }            this.touchProjection = new TouchProjection(clientId);            this.touchProjection.start();        }    }    public void stopMinicap() {        if (this.screenAndroidProjection != null) {            this.screenAndroidProjection.close();        }        if (this.screenIOSProjection != null) {            this.screenIOSProjection.close();        }    }    public void stopMinitouch() {        if (this.touchProjection != null) {            this.touchProjection.close();        }    }    public void keyevent(Integer code) {        if (this.touchProjection != null) this.touchProjection.sendCode(code);    }    public void touch( String command) {        if (this.touchProjection != null) this.touchProjection.sendEvent(command);    }    /**     * 等待时，关闭logcat     * @param     */    public void stopLogcat() {        if(this.dLogger != null){            this.dLogger.close();        }    }    /**     * 再次发送命令下来时，重新打开logcat     * @param command     */    public synchronized void startLogcat(String command) {        this.stopLogcat();        this.isLogcatWaitting = true;        if(StringUtils.isNotEmpty(command)) {            Map filter_params = JSON.parseObject(command, Map.class);            if(filter_params != null) {                String tag = (String) filter_params.getOrDefault("tag", "");                String pid = (String) filter_params.getOrDefault("pid", "");                Log.LogLevel level = Log.LogLevel.getByLetterString((String) filter_params.getOrDefault("level", "E"));                String message = (String) filter_params.get("message");                logCatFilter = new LogCatFilter(tag, message, pid, level);            }        }        if(dLogger == null || !dLogger.isRunning()) {            dLogger = new DLogger(this.clientId, this);            dLogger.start();        }    }    public void onLog(byte[] bytes) {        try {            List<LogcatMessageRequest> logLines = new ArrayList<>();            String logstr = new String(bytes, StandardCharsets.UTF_8).replace("\0", "");            String[] lines = logstr.split("\n");            for(String line : lines) {                // 正则解析 logstr                Matcher matcher = logcatPattern.matcher(line);                if(matcher.matches()){                    String L_data = matcher.group(1);                    String L_time = matcher.group(2);                    String L_process = matcher.group(3);                    String L_thread = matcher.group(4);                    Log.LogLevel L_level = Log.LogLevel.getByLetterString(matcher.group(5));                    if(L_level == null) {                        continue;                    }                    String L_tag = matcher.group(6);                    String L_message = matcher.group(7);                    LogCatMessage logCatMessage = new LogCatMessage(L_data, L_time, L_process, L_thread, L_level, L_tag, L_message);                    if(logCatFilter.matches(logCatMessage)) {                        LogcatMessageRequest messageRequest = LogcatMessageRequest.newBuilder()                                                                .setData(L_data)                                                                .setTime(L_time)                                                                .setPid(L_process)                                                                .setThread(L_thread)                                                                .setLevel(L_level.getStringValue())                                                                .setTag(L_tag)                                                                .setMessage(L_message)                                                                .build();                        logLines.add(messageRequest);                    }                }            }            LogcatRequest request = LogcatRequest.newBuilder()                    .setSerial(clientId)                    .addAllMessages(logLines)                    .build();            Gvice.deviceService(this.channel).logcat(request);        }catch (Exception e){            log.error(e.getMessage());        }finally {        }    }    /**     *@Description: 开始执行测试任务     *@Param: [command]     *@Return: void     *@Author: wen     *@Date: 2018/4/9     */    public void startFunctionalTask(String command) {        log.info("设备 {}，开始任务 {}", clientId, command);//        stopCurrentTask();        switchADBKeyBoard();        RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);//        defaultVideoRecorderStart(cmd.getTaskCode());        Future<?> future = es.get().submit(() -> {            Thread currentThread = Thread.currentThread();            setCurrentTaskThread(currentThread);            task = new FunctionalTestTask(cmd, this);            task.start();        });    }    /**     * 兼容测试     */    public void startCompatibilityTask(String command) {        log.info("设备 {}，开始兼容任务 {}", clientId, command);//        stopCurrentTask();        RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);//        defaultVideoRecorderStart(cmd.getTaskCode());        Future<?> future = es.get().submit(() -> {            Thread currentThread = Thread.currentThread();            setCurrentTaskThread(currentThread);            task = new CompatibilityAndroidTestTask(cmd, this);            task.start();        });    }    /**     * 遍历测试     */    public void startCrawlerTask(String command) {        log.info("设备 {}，开始遍历任务 {}", clientId, command);//        stopCurrentTask();        RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);//        defaultVideoRecorderStart(cmd.getTaskCode());        Future<?> future = es.get().submit(() -> {            Thread currentThread = Thread.currentThread();            setCurrentTaskThread(currentThread);            task = new CrawlerTestTask(cmd, this);            task.start();        });    }    /**     *@Description: 取消在该设备正在执行的任务     *@Param: [command]     *@Return: void     *@Author: wen     *@Date: 2018/4/9     */    public void cancelTask(Long taskCode) {        if(task != null){            RemoteRunCommand cmd = task.getCMD();            if(taskCode.equals(cmd.getTaskCode())) {                task.terminate();//                stopCurrentTask();                task = null;            }        }    }    private void stopCurrentTask() {        if(currentTaskThread.get() != null){            while (currentTaskThread.get().isAlive()) {                currentTaskThread.get().stop();                try {                    TimeUnit.MILLISECONDS.sleep(50);                } catch (InterruptedException e) {                }            }        }    }    private void setCurrentTaskThread(Thread currentThread) {        currentTaskThread.set(currentThread);        currentThread.setUncaughtExceptionHandler((t1, e) -> {            if (e instanceof ThreadDeath || e instanceof IllegalMonitorStateException) {                e.printStackTrace();                es.get().shutdownNow();                es.set(Executors.newSingleThreadExecutor());            }        });    }    /**     *@Description: 任务执行完成的回调     *@Param: []     *@Return: void     *@Author: wen     *@Date: 2018/7/10     */    @Override    public void taskFinish() {        if(this.screenAndroidProjection != null) {            this.screenAndroidProjection.close();            log.info("关闭屏幕映射 {} {} ", clientInfo.getBrand(), clientInfo.getDeviceId());            try {                TimeUnit.SECONDS.sleep(5);            } catch (InterruptedException e) {            }        }        switchOriginalKeyboard();    }    @Override    public byte[] takeFrame() {        if(DeviceType.ANDROID.equals(this.deviceType)){            if(this.screenAndroidProjection != null && this.screenAndroidProjection.isRunning()) {                return this.screenAndroidProjection.frame();            }        }        return null;    }    @Override    public String getVideoFile() {        return this.videoOutputFile;    }    private void switchADBKeyBoard() {        if(DeviceType.ANDROID.equals(deviceType)) {            es.get().submit(() -> {                // 检查输入法是否已经安装                boolean isInstall = ADBCommandUtils.isInstalledBasepackage(clientId, "com.android.adbkeyboard");                if(!isInstall){                    // install 支持中文输入的输入法                    String resourcesPath = Config.getString("distest.agent.resources");                    String keyboardPath = resourcesPath + File.separator + Constant.getKeyboardService();                    if(Files.exists(Paths.get(keyboardPath))){                        ADBCommandUtils.installApp(clientId, keyboardPath);                    }                }else{                    ADBCommandUtils.switchADBKeyBoard(clientId);                }            });        }    }    private void switchOriginalKeyboard() {        if(DeviceType.ANDROID.equals(deviceType)) {            ADBCommandUtils.switchOriginalKeyboard(clientId);        }    }}