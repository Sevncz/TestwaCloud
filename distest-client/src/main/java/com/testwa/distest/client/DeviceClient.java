package com.testwa.distest.client;import com.alibaba.fastjson.JSON;import com.alibaba.fastjson.JSONObject;import com.github.cosysoft.device.android.AndroidDevice;import com.google.protobuf.ByteString;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.distest.client.android.AndroidHelper;import com.testwa.distest.client.android.ADBCommandUtils;import com.testwa.distest.client.component.Constant;import com.testwa.distest.client.component.appium.utils.Config;import com.testwa.distest.client.component.executor.AbstractTestTask;import com.testwa.distest.client.component.executor.HGTestTask;import com.testwa.distest.client.component.executor.JRAndroidTestTask;import com.testwa.distest.client.component.logcat.Logcat;import com.testwa.distest.client.component.logcat.LogcatListener;import com.testwa.distest.client.component.minicap.MinicapImgSender;import com.testwa.distest.client.component.minicap.AMinicapServer;import com.testwa.distest.client.component.minicap.MinicapListener;import com.testwa.distest.client.component.minicap.MinicapServer;import com.testwa.distest.client.component.minicap.ios.IMinicapServer;import com.testwa.distest.client.component.minitouch.Minitouch;import com.testwa.distest.client.component.minitouch.MinitouchListener;import com.testwa.distest.client.callback.DefaultObserver;import com.testwa.distest.client.exception.DeviceNotReadyException;import com.testwa.distest.client.ios.IOSDeviceUtil;import com.testwa.distest.client.model.IOSDevice;import com.testwa.distest.client.service.Gvice;import io.grpc.Channel;import io.rpc.testwa.device.DeviceType;import io.rpc.testwa.device.LogcatRequest;import io.rpc.testwa.device.ScreenCaptureRequest;import io.rpc.testwa.push.ClientInfo;import io.rpc.testwa.push.PushGrpc;import io.rpc.testwa.push.Status;import io.rpc.testwa.push.TopicInfo;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import java.io.File;import java.nio.file.Files;import java.nio.file.Paths;import java.util.concurrent.*;import java.util.concurrent.atomic.AtomicReference;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-08 16:27 **/@Slf4jpublic class DeviceClient implements MinicapListener, MinitouchListener, LogcatListener {    private final PushGrpc.PushStub asyncStub;    private final PushGrpc.PushBlockingStub blockingStub;    private final Channel channel;    private final String clientId;    private final String userToken;    private final ClientInfo clientInfo;    static final int DATA_TIMEOUT = 100; //ms    private boolean isWaitting = false;    private boolean isLogcatWaitting = false;    private BlockingQueue<ImageData> dataQueue = new LinkedBlockingQueue<>();    private BlockingQueue<ImageData> toVideoDataQueue = new LinkedBlockingQueue<>();    private String resourcesPath;  // minicap和minitouch存放的路径    private DeviceType deviceType;    private MinicapServer minicapServer = null;    private MinicapImgSender minicapImgSender = null;    private Minitouch minitouch = null;    private Logcat logcat = null;    private AbstractTestTask task = null;    private String minicapCommand;    private boolean isVideo;    private int buildClientInfoMaxTime = 10;    private final AtomicReference<Thread> currentTaskThread;    private final AtomicReference<ExecutorService> es;    private ExecutorService onJPGService;    public DeviceClient(String deviceId, Channel channel, String userToken, DeviceType deviceType) throws DeviceNotReadyException {        this.deviceType = deviceType;        this.clientId = deviceId;        this.userToken = userToken;        this.channel = channel;        this.asyncStub = PushGrpc.newStub(channel);        this.blockingStub = PushGrpc.newBlockingStub(channel);        this.resourcesPath = Config.getString("distest.agent.resources");        this.clientInfo = buildClientInfo();        this.onJPGService = Executors.newFixedThreadPool(10);        ExecutorService taskWorker = Executors.newFixedThreadPool(3);        es = new AtomicReference<>();        es.set(taskWorker);        currentTaskThread = new AtomicReference<>();        if(DeviceType.ANDROID.equals(deviceType)) {            es.get().submit(() -> {                // 检查输入法是否已经安装                boolean isInstall = ADBCommandUtils.isInstalledBasepackage(clientId, "com.android.adbkeyboard");                if(!isInstall){                    // install 支持中文输入的输入法                    String keyboardPath = resourcesPath + File.separator + Constant.getKeyboardService();                    if(Files.exists(Paths.get(keyboardPath))){                        ADBCommandUtils.installApp(clientId, keyboardPath);                    }                }else{                    ADBCommandUtils.switchADBKeyBoard(clientId);                }            });        }        es.get().submit(() -> {            while(isWaitting) {                // 挑选没有超时的图片                ImageData d = getUsefulImage();                if(d != null){                    sendImage(d.data);                }            }        });        log.info("设备 {} {} 初始化 ", clientInfo.getBrand(), clientInfo.getModel());    }    /**     * grpc     */    public void resendRegister(DefaultObserver defaultObserver) {        this.asyncStub.registerToServer(this.clientInfo, defaultObserver);    }    public void registerToServer(){        this.asyncStub.registerToServer(this.clientInfo, new DefaultObserver(this.clientId, this));    }    public String subscribe(String topic){        TopicInfo topicInfo = TopicInfo.newBuilder().setTopicName(topic).setClientInfo(this.clientInfo).build();        Status status = this.blockingStub.subscribe(topicInfo);        return status.getStatus();    }    public String cancel(String topic){        TopicInfo topicInfo = TopicInfo.newBuilder().setTopicName(topic).setClientInfo(this.clientInfo).build();        Status status = this.blockingStub.cancel(topicInfo);        return status.getStatus();    }    public String logoutFromServer(){        Status status = this.blockingStub.logoutFromServer(this.clientInfo);        return status.getStatus();    }    public String getClientId() {        return clientId;    }    public ClientInfo buildClientInfo() throws DeviceNotReadyException {        if(DeviceType.ANDROID.equals(this.deviceType)) {            AndroidDevice dev = AndroidHelper.getInstance().getAndroidDevice(this.clientId);            if(dev != null && dev.getDevice().isOnline()){                String brand = dev.runAdbCommand("shell getprop ro.product.brand");                if(StringUtils.isBlank(brand)){                    if(buildClientInfoMaxTime <= 0){                        throw new DeviceNotReadyException("无法获取设备属性");                    }                    buildClientInfoMaxTime--;                    try {                        TimeUnit.MILLISECONDS.sleep(1000);                    } catch (InterruptedException e) {                        e.printStackTrace();                    }                    return buildClientInfo();                }                String cpuabi = dev.runAdbCommand("shell getprop ro.product.cpu.abi");                String sdk = dev.runAdbCommand("shell getprop ro.build.version.sdk");                String host = dev.runAdbCommand("shell getprop ro.build.host");                String model = dev.runAdbCommand("shell getprop ro.product.model");                String version = dev.runAdbCommand("shell getprop ro.build.version.release");                String density = dev.getDevice().getDensity() + "";                String targetPlatform = "Android";                if (dev.getTargetPlatform() != null) {                    targetPlatform = dev.getTargetPlatform().formatedName();                }                String width = "";                String height = "";                // 获取设备屏幕物理分辨率                String output = dev.runAdbCommand("shell wm size");                if (output != null && !output.isEmpty()) {                    String overrideSizeFlag = "Override";                    if(output.contains(overrideSizeFlag)){                        output = output.split("\n")[0].trim();                    }                    String sizeStr = output.split(":")[1].trim();                    width = sizeStr.split("x")[0].trim();                    height = sizeStr.split("x")[1].trim();                }                return ClientInfo.newBuilder()                        .setDeviceId(dev.getSerialNumber())                        .setBrand(brand)                        .setCpuabi(cpuabi)                        .setDensity(density)                        .setHeight(height)                        .setWidth(width)                        .setHost(host)                        .setModel(model)                        .setOsName(targetPlatform)                        .setSdk(sdk)                        .setUserFlag(userToken)                        .setVersion(version)                        .build();            }else{                throw new DeviceNotReadyException("设备不在线，无法初始化");            }        }else{            String cpu = IOSDeviceUtil.getCPUArchitecture(clientId);            String productType = IOSDeviceUtil.getProductType(clientId);            String productVersion = IOSDeviceUtil.getProductVersion(clientId);            return ClientInfo.newBuilder()                    .setDeviceId(clientId)                    .setBrand("Apple")                    .setCpuabi(cpu)                    .setModel(productType)                    .setOsName("iOS")                    .setUserFlag(userToken)                    .setVersion(productVersion)                    .build();        }    }    public void stop() {        // 关闭组件        if (minitouch != null) {            minitouch.kill();        }        if (minicapServer != null) {            minicapServer.stop();        }        if (minicapImgSender != null) {            minicapImgSender.stop();        }        if (task != null) {            task.kill();        }        // 关闭录制        isVideo = false;        isWaitting = false;        this.es.get().shutdown();    }    public void restart() {        // 关闭组件        if (minitouch != null) {            minitouch.kill();        }        if (minicapServer != null) {            minicapServer.stop();        }        if(StringUtils.isNotBlank(minicapCommand)){            startMinicap(minicapCommand);            startMinitouch();        }    }    /**     * ImageData     */    public static class ImageData {        ImageData(byte[] d) {            timesp = System.currentTimeMillis();            data = d;        }        long timesp;        byte[] data;    }    /**     * minicapServer     */    @Override    public void onJPG(byte[] data) {        // for video queue//        toVideoDataQueue.add(new LocalClient.ImageData(data));        // for pic queue        if (isWaitting) {            if (dataQueue.size() > 0) {                dataQueue.add(new ImageData(data));                if(dataQueue.size() > 50) {                    dataQueue.poll();                }            } else {                sendImage(data);            }//            isWaitting = false;        } else {            clearObsoleteImage();//            dataQueue.add(new ImageData(data));        }    }    public void setWaitting(boolean waitting) {        isWaitting = waitting;        if(isWaitting) {            trySendImage();        }else{            clearObsoleteImage();        }    }    private void trySendImage() {        ImageData d = getUsefulImage();        if (d != null) {            sendImage(d.data);        }    }    private ImageData getUsefulImage() {        long curTS = System.currentTimeMillis();        // 挑选没有超时的图片        ImageData d = null;        while (true) {            d = dataQueue.poll();            // 如果没有超时，或者超时了但是最后一张图片，也发送给客户端            if (d == null || curTS - d.timesp < DATA_TIMEOUT || dataQueue.size() == 0) {                break;            }        }        return d;    }    private void sendImage(byte[] data) {        try {            ScreenCaptureRequest request = ScreenCaptureRequest.newBuilder()                    .setImg(ByteString.copyFrom(data))                    .setName("xxx")                    .setSerial(this.clientId)                    .build();            Gvice.deviceService(this.channel).screen(request);        }catch (Exception e){            log.error(e.getMessage());        }finally {        }    }    private void clearObsoleteImage() {        ImageData d = dataQueue.peek();        long curTS = System.currentTimeMillis();        while (d != null) {            if (curTS - d.timesp > DATA_TIMEOUT) {                dataQueue.poll();                d = dataQueue.peek();            } else {                break;            }        }    }    /**     * minitouch     */    @Override    public void onStartup(Minitouch minitouch, boolean success) {    }    @Override    public void onClose(Minitouch minitouch) {        // 检查设备是否在线        AndroidDevice dev = AndroidHelper.getInstance().getAndroidDevice(this.clientId);        stopMinitouch();        if(dev != null){            if(dev.getDevice().isOnline()){                startMinitouch();                return;            }        }        // TODO 通知服务器        log.error("Minitouch 连接断开");    }    /**     * logcat     */    @Override    public void onStartup(Logcat logcat, boolean success) {    }    @Override    public void onClose(Logcat logcat) {    }    @Override    public void onLog(Logcat logcat, byte[] data) {        if (isLogcatWaitting) {            sendLog(data);        }    }    private void sendLog(byte[] data) {        log.debug(String.valueOf(data.length));        try {            LogcatRequest request = LogcatRequest.newBuilder()                    .setSerial(clientId)                    .setContent(ByteString.copyFrom(data))                    .build();            Gvice.deviceService(this.channel).logcat(request);        }catch (Exception e){            log.error(e.getMessage());        }finally {        }    }    public void startMinicap(String command) {        long startTime = System.currentTimeMillis();        log.warn("{} ready to start minicap, time: {}", clientInfo.getModel(), startTime);        // 获取请求的配置        JSONObject obj = JSON.parseObject(command);        Float scale = obj.getFloat("scale");        Float rotate = obj.getFloat("rotate");//        scale = 0.2f;        if (scale == null) {scale = 0.3f;}        if (scale < 0.01) {scale = 0.01f;}        if (scale > 1.0) {scale = 1.0f;}        if (rotate == null) { rotate = 0.0f; }        minicapCommand = command;        if(this.minicapServer != null){            if(this.minicapImgSender != null){                this.minicapImgSender.stop();            }            this.minicapServer.restart(scale, rotate.intValue());        }else{            if(DeviceType.ANDROID.equals(this.deviceType)) {                AMinicapServer minicap = new AMinicapServer(clientId, resourcesPath, scale, rotate.intValue());                minicap.start();                this.minicapServer = minicap;            }else{                IMinicapServer minicap = new IMinicapServer(clientId);                minicap.start();                this.minicapServer = minicap;            }        }        MinicapImgSender sender = new MinicapImgSender(this.minicapServer.getPort());        sender.addEventListener(this);        sender.start();        this.minicapImgSender = sender;        long endTime = System.currentTimeMillis();        log.info("{} 启动 minicapServer 端口 {} 参数 {} 耗时 {}ms", clientInfo.getModel(), this.minicapServer.getPort(), command, endTime - startTime);    }    public void startMinitouch() {        if(DeviceType.ANDROID.equals(this.deviceType)) {            if (minitouch != null) {                minitouch.kill();            }            log.info("启动 minitouch");            Minitouch minitouch = new Minitouch(clientId, resourcesPath);            minitouch.addEventListener(this);            minitouch.start();            this.minitouch = minitouch;        }    }    public void stopMinicap() {        if (this.minicapServer != null) {            this.minicapServer.stop();        }        if (this.minicapImgSender != null) {            this.minicapImgSender.stop();        }    }    public void stopMinitouch() {        if (minitouch != null) {            minitouch.kill();        }    }    public void keyevent(String command) {        int k = Integer.parseInt(command);        if (minitouch != null) minitouch.sendKeyEvent(k);    }    public void touch( String command) {        if (minitouch != null) minitouch.sendEvent(command);    }    /**     * 等待时，关闭logcat     * @param     */    public void stopLogcat() {        if(this.logcat != null){            this.logcat.close();        }    }    /**     * 再次发送命令下来时，重新打开logcat     * @param command     */    public void startLogcat(String command) {        if(this.logcat != null){            this.logcat.close();        }        this.isLogcatWaitting = true;        Logcat l = new Logcat(this.clientId, command);        l.addEventListener(this);        l.start();        this.logcat = l;    }    /**     *@Description: 开始执行测试任务     *@Param: [command]     *@Return: void     *@Author: wen     *@Date: 2018/4/9     */    public void startTask(String command) {        log.info("设备 {}，开始任务 {}", clientId, command);        stopCurrentTask();        Future<?> future = es.get().submit(() -> {            Thread currentThread = Thread.currentThread();            setCurrentTaskThread(currentThread);            RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);            task = new HGTestTask(cmd);            task.start();        });    }    /**     *@Description: 取消在该设备正在执行的任务     *@Param: [command]     *@Return: void     *@Author: wen     *@Date: 2018/4/9     */    public void cancelTask(Long taskCode) {        if(task != null){            RemoteRunCommand cmd = task.getCMD();            if(taskCode.equals(cmd.getTaskCode())) {                task.kill();                stopCurrentTask();                task = null;            }        }    }    private void stopCurrentTask() {        if(currentTaskThread.get() != null){            while (currentTaskThread.get().isAlive()) {                currentTaskThread.get().stop();                try {                    TimeUnit.MILLISECONDS.sleep(50);                } catch (InterruptedException e) {                }            }        }    }    /**     * 兼容测试     */    public void startJRTask(String command) {        log.info("设备 {}，开始兼容任务 {}", clientId, command);        stopCurrentTask();        Future<?> future = es.get().submit(() -> {            Thread currentThread = Thread.currentThread();            setCurrentTaskThread(currentThread);            RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);            task = new JRAndroidTestTask(cmd);            task.start();        });    }    private void setCurrentTaskThread(Thread currentThread) {        currentTaskThread.set(currentThread);        currentThread.setUncaughtExceptionHandler((t1, e) -> {            if (e instanceof ThreadDeath || e instanceof IllegalMonitorStateException) {                e.printStackTrace();                es.get().shutdownNow();                es.set(Executors.newSingleThreadExecutor());            }        });    }}