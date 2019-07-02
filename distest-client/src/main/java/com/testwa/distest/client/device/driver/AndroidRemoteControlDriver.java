package com.testwa.distest.client.device.driver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.ddmlib.IDevice;
import com.testwa.core.cmd.AppInfo;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.android.*;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.component.appium.utils.Config;
import com.testwa.distest.client.component.debug.AndroidDebugServer;
import com.testwa.distest.client.component.executor.task.*;
import com.testwa.distest.client.component.logcat.DLogger;
import com.testwa.distest.client.component.minicap.ScreenAndroidProjection;
import com.testwa.distest.client.component.minitouch.TouchAndroidProjection;
import com.testwa.distest.client.component.port.SocatPortProvider;
import com.testwa.distest.client.component.port.TcpIpPortProvider;
import com.testwa.distest.client.component.stfagent.StfAgentClient;
import com.testwa.distest.client.component.stfagent.StfAPKServer;
import com.testwa.distest.client.component.stfagent.StfServiceClient;
import com.testwa.distest.client.device.listener.AndroidComponentServiceRunningListener;
import com.testwa.distest.client.device.listener.callback.IRemoteCommandCallBack;
import com.testwa.distest.client.device.listener.callback.RemoteCommandCallBackUtils;
import com.testwa.distest.client.device.manager.DeviceInitException;
import com.testwa.distest.client.device.remote.DeivceRemoteApiClient;
import com.testwa.distest.client.download.Downloader;
import com.testwa.distest.client.exception.DownloadFailException;
import com.testwa.distest.client.model.AgentInfo;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.jadb.JadbDevice;
import com.testwa.distest.jadb.JadbException;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.device.DeviceType;
import io.rpc.testwa.push.ClientInfo;
import io.rpc.testwa.push.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Android设备驱动
 *
 * @author wen
 * @create 2019-05-23 18:52
 */
@Slf4j
public class AndroidRemoteControlDriver implements IDeviceRemoteControlDriver, StreamObserver<Message> {
    private volatile ConcurrentHashMap<Message.Topic, IRemoteCommandCallBack> cache = new ConcurrentHashMap<>();

    /*------------------------------------------环境信息----------------------------------------------------*/
    /**
     * appium 相关包
     */
    private static final String UNICODEIME_PACKAGE = "io.appium.android.ime";
    private static final String UI2_SERVER_PACKAGE = "io.appium.uiautomator2.server";
    private static final String UI2_DEBUG_PACKAGE = "io.appium.uiautomator2.server.test";
//    private static final String SELENDROID_SERVER_PACKAGE = "io.selendroid.server";
//    private static final String SETTINGS_PACKAGE = "io.appium.settings.Settings";
//    private static final String UNLOCK_PACKAGE = "io.appium.unlock";
    /**
     * stf 相关包
     */
    private static final String STF_SERVICE_PACKAGE = "jp.co.cyberagent.stf";
    /**
     * keyboardservice 相关包
     */
    private static final String KEYBOARD_SERVICE_PACKAGE = "com.android.adbkeyboard";

    /*------------------------------------------设备信息----------------------------------------------------*/

    private final static DeviceType TYPE = DeviceType.ANDROID;
    private final IDeviceRemoteControlDriverCapabilities capabilities;
    /**
     * 屏幕显示标准大小
     */
    private static final int BASE_WIDTH = 720;
    private float defaultScale = 0.5f;
    private float userScale = 0.5f;
    private int defaultRotation = 0;
    private int userRotation = 0;
    // 设置画质
    private static final int QUALITY = 25;

    private final JadbDevice device;
    private ClientInfo clientInfo;

    private AndroidComponentServiceRunningListener listener;
    private DeivceRemoteApiClient api;

    private int buildClientInfoMaxTime = 10;

    /*------------------------------------------COMPONENT----------------------------------------------------*/

    private ScreenAndroidProjection screenProjection;
    private TouchAndroidProjection touchProjection;
    private StfAPKServer stfAgentServer;
    private StfAgentClient stfAgentClient;
    private StfServiceClient stfServiceClient;


    /*------------------------------------------LOGCAT----------------------------------------------------*/

    private DLogger dLogger;


    /*------------------------------------------远程DEBUG----------------------------------------------------*/

    private int tcpipPort;
    private int socatPort;
    private AndroidDebugServer androidDebugServer;

    /**
     * 毫秒
     * 超过该时间再断开，即算断开
     */
    private static final Long TCPIP_TIMEOUT = 2*1000L;
    /*------------------------------------------远程任务----------------------------------------------------*/

    private AbstractTestTask task = null;

    private AtomicInteger connectRetryTime = new AtomicInteger(0);

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public AndroidRemoteControlDriver(IDeviceRemoteControlDriverCapabilities capabilities){
        this.capabilities = capabilities;

        this.api = ApplicationContextUtil.getBean(DeivceRemoteApiClient.class);

        this.device = JadbDeviceManager.getJadbDevice(capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.DEVICE_ID));

        this.listener = new AndroidComponentServiceRunningListener(capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.DEVICE_ID), api);

        this.tcpipPort = TcpIpPortProvider.pullPort();

        this.socatPort = SocatPortProvider.pullPort();
    }

    @Override
    public void deviceInit() throws DeviceInitException {
        initTcpipCommand();
        clientInfo = buildClientInfo();
        // 注册到server
        register();
    }

    @Override
    public void register() {
        this.api.registerToServer(clientInfo, this);
    }

    @Override
    public boolean isOnline() {
        try {
            return JadbDevice.State.Device.equals(device.getState());
        } catch (IOException | JadbException e) {
            return false;
        }
    }

    @Override
    public void startProjection(String command) {
        // 获取请求的配置
        JSONObject obj = JSON.parseObject(command);
        Float scale = obj.getFloat("scale");
        // 设置缩放
        if (scale == null) {scale = defaultScale;}
        if (scale < 0.01) {scale = 0.01f;}
        if (scale > 1.0) {scale = 1.0f;}
        // 设置旋转
        Integer rotate = obj.getInteger("rotate");
        if (rotate != null) {
            userRotation = rotate;
        }
        restartProjection(userRotation);

        log.info("[{}] 屏幕已启动", this.device.getSerial());
    }

    private void restartProjection(Integer rotate) {
        stopProjection();
        // 重建新的进程
        this.touchProjection = new TouchAndroidProjection(this.device.getSerial(), this.capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.RESOURCE_PATH));
        this.touchProjection.start();

        this.screenProjection = new ScreenAndroidProjection(this.device.getSerial(), this.capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.RESOURCE_PATH), this.listener);
        this.screenProjection.setRotate(rotate);
        this.screenProjection.setQuality(QUALITY);
        this.screenProjection.start();
    }

    @Override
    public void stopProjection() {
        if(this.screenProjection != null) {
            this.screenProjection.close();
        }
        if(this.touchProjection != null) {
            this.touchProjection.close();
        }
    }

    @Override
    public void rate(String command) {
        JSONObject rateCommand = JSON.parseObject(command);
        Integer rate = rateCommand.getInteger("rate");
        this.listener.rate(rate);
    }

    @Override
    public void startLog(String command) {
        // logcat 解析器
        this.listener.buildLogcatFilter(command);
        // 启动 logcat 日志服务
        if(dLogger == null) {
            dLogger = new DLogger(this.device.getSerial(), DeviceType.ANDROID, this.listener);
            dLogger.start();
        }else{
            if(!dLogger.isRunning()) {
                dLogger.close();
                dLogger = new DLogger(this.device.getSerial(), DeviceType.ANDROID, this.listener);
                dLogger.start();
            }
        }
        this.listener.setLogWait(true);
        log.info("[{}] {} 日志已启动", this.device.getSerial());
    }

    @Override
    public void waitLog() {
        this.listener.setLogWait(true);
    }

    @Override
    public void notifyLog() {
        this.listener.setLogWait(false);
    }

    @Override
    public void stopLog() {
        if(dLogger != null){
            dLogger.close();
        }
    }

    @Override
    public void startRecorder() {

    }

    @Override
    public void stopRecorder() {

    }

    @Override
    public DeviceType getType() {
        return TYPE;
    }

    @Override
    public String getDeviceId() {
        return device.getSerial();
    }

    @Override
    public void destory() {

        this.stopProjection();
        this.stopLog();
        this.stopRecorder();
        this.stopStf();
    }

    @Override
    public boolean isRealOffline() {
        try {
            this.device.getState();
            return false;
        } catch (IOException | JadbException e) {
            try {
                TimeUnit.MILLISECONDS.sleep(TCPIP_TIMEOUT);
            } catch (InterruptedException e1) {

            }
        }
        try {
            this.device.getState();
            return false;
        } catch (IOException | JadbException e) {
            return true;
        }
    }

    @Override
    public void debugStart() {
        debugStop();
        this.androidDebugServer = new AndroidDebugServer(this.device.getSerial(), this.tcpipPort, this.socatPort, this.capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.RESOURCE_PATH));
        this.androidDebugServer.start();
    }

    @Override
    public void debugStop() {
        if(this.androidDebugServer != null && this.androidDebugServer.isRunning()) {
            this.androidDebugServer.stop();
        }
    }

    @Override
    public void keyevent(int keyCode) {
        if (this.stfAgentClient != null && this.stfAgentClient.isRunning()) {
            this.stfAgentClient.onKeyEvent(keyCode);
        }
    }

    @Override
    public void inputText(String cmd) {

        if(hasChineseByRange(cmd)) {
            ADBCommandUtils.switchADBKeyBoard(this.device.getSerial());
            ADBCommandUtils.inputTextADBKeyBoard(this.device.getSerial(), cmd, 5000L);
        }else{
            // 使用stfservice处理非中文字符
            if (this.stfAgentClient != null && this.stfAgentClient.isRunning()) {
                this.stfAgentClient.onType(cmd);
            }
        }
    }


    /**
     * 是否包含汉字<br>
     * 根据汉字编码范围进行判断<br>
     * CJK统一汉字（不包含中文的，。《》（）“‘’”、！￥等符号）<br>
     *
     * @param str
     * @return
     */
    private boolean hasChineseByRange(String str) {
        if (str == null) {
            return false;
        }
        char[] ch = str.toCharArray();
        for (char c : ch) {
            if (c >= 0x4E00 && c <= 0x9FBF) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void touch(String cmd) {
        if (this.touchProjection != null && this.touchProjection.isRunning()) {
            this.touchProjection.sendEvent(cmd);
        }
    }

    @Override
    public void swip(String cmd) {
        log.error("iOS event {}", cmd);
    }

    @Override
    public void tap(String cmd) {
        log.error("iOS event {}", cmd);

    }

    @Override
    public void tapAndHold(String cmd) {
        log.error("iOS event {}", cmd);
    }

    @Override
    public void setRotation(String cmd) {
        if(this.stfAgentClient != null) {
            int rotation = Integer.parseInt(cmd);
            this.stfAgentClient.setRotation(rotation);
        }
    }

    @Override
    public void installApp(String command) {

        AppInfo appInfo = JSON.parseObject(command, AppInfo.class);
        String distestApiWeb = Config.getString("cloud.web.url");

        String appUrl = String.format("%s/app/%s", distestApiWeb, appInfo.getPath());
        String appLocalPath = Constant.localAppPath + File.separator + appInfo.getMd5() + File.separator + appInfo.getFileName();

        executorService.submit(() -> {
            // 检查是否有和该app md5一致的
            try {
                Downloader d = new Downloader();
                d.start(appUrl, appLocalPath);
            } catch (DownloadFailException | IOException e) {
                e.printStackTrace();
            }
            log.info("[{}] Install APK {} to {} ", this.device.getSerial(), appLocalPath, device.getSerial());

            ADBCommandUtils.installApp(device.getSerial(), appLocalPath);
            ADBCommandUtils.launcherApp(device.getSerial(), appLocalPath);

        });
    }

    @Override
    public void uninstallApp(String basePackage) {
        ADBTools.uninstallApp(device.getSerial(), basePackage);
    }

    @Override
    public void openWeb(String cmd) {
        ADBTools.openWeb(this.device.getSerial(), cmd);
    }

    @Override
    public void startCrawlerTask(String command) {
        log.info("[{}] 开始遍历任务 {}", device.getSerial(), command);
        RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);
//        defaultVideoRecorderStart(cmd.getTaskCode());
        task = new CrawlerTestTask(cmd, listener);
        TaskDispatcher.getInstance().submit(task);
    }

    @Override
    public void startCompatibilityTask(String command) {
        log.info("[{}] 开始兼容任务 {}", device.getSerial(), command);
        RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);
//        defaultVideoRecorderStart(cmd.getTaskCode());
        task = new CompatibilityAndroidTestTask(cmd, listener);
        TaskDispatcher.getInstance().submit(task);
    }

    @Override
    public void startFunctionalTask(String command) {
        log.info("[{}] 开始任务 {}", device.getSerial(), command);
//        switchADBKeyBoard();
        RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);
//        defaultVideoRecorderStart(cmd.getTaskCode());
        task = new FunctionalTestTask(cmd, listener);
        TaskDispatcher.getInstance().submit(task);
    }

    @Override
    public void stopTask(String taskCode) {
        if(task != null){
            RemoteRunCommand cmd = task.getCMD();
            if(taskCode.equals(String.valueOf(cmd.getTaskCode()))) {
                task.terminate();
                task = null;
            }
        }
    }

    private ClientInfo buildClientInfo() throws DeviceInitException {
        AgentInfo agentInfo = AgentInfo.getAgentInfo();
        log.info("[{}] Register device to server, agentInfo: {} ", this.device.getSerial(), agentInfo.toString());
        try {
            JadbDevice.State state = this.device.getState();
            if(JadbDevice.State.Device.equals(state)) {
                String brand = ADBTools.getProp(this.device.getSerial(), "ro.product.brand");
                if(StringUtils.isBlank(brand)){
                    if(buildClientInfoMaxTime <= 0){
                        log.error("[{}] Register fail, Can't get ro.product.brand ", this.device.getSerial());
                        throw new DeviceInitException("无法获取设备["+this.device.getSerial()+"]属性");
                    }
                    buildClientInfoMaxTime--;
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return buildClientInfo();
                }
                String cpuabi = ADBTools.getProp(this.device.getSerial(), IDevice.PROP_DEVICE_CPU_ABI);
                String sdk = ADBTools.getProp(this.device.getSerial(), IDevice.PROP_BUILD_API_LEVEL);
                String host = ADBTools.getProp(this.device.getSerial(), "ro.build.host");
                String model = ADBTools.getProp(this.device.getSerial(), IDevice.PROP_DEVICE_MODEL);
                String version = ADBTools.getProp(this.device.getSerial(), IDevice.PROP_BUILD_VERSION);
                String density = ADBTools.getProp(this.device.getSerial(), IDevice.PROP_DEVICE_DENSITY);
                PhysicalSize size = ADBCommandUtils.getPhysicalSize(device.getSerial());
                // 设置默认scale
                if(size.getWidth() >= BASE_WIDTH) {
                    double rate = BASE_WIDTH / (size.getWidth()*1.0);
                    this.defaultScale = BigDecimal.valueOf(rate).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                }
                String width = String.valueOf(size.getWidth());
                String height = String.valueOf(size.getHeight());

                // 检查 uiautomator2、stfagent 等组件的安装情况
//                boolean stfagentInstall = ADBCommandUtils.isInstalledBasepackage(device.getSerial(), STF_SERVICE_PACKAGE);
//                if(!stfagentInstall) {
//                    log.warn("{} 未安装 {}", device.getSerial(), STF_SERVICE_PACKAGE);
//                }
//                boolean appiumServerInstall = ADBCommandUtils.isInstalledBasepackage(device.getSerial(), UI2_SERVER_PACKAGE);
//                if(!appiumServerInstall) {
//                    log.warn("{} 未安装 {}", device.getSerial(), UI2_SERVER_PACKAGE);
//                }
//                boolean appiumDebugInstall = ADBCommandUtils.isInstalledBasepackage(device.getSerial(), UI2_DEBUG_PACKAGE);
//                if(!appiumDebugInstall) {
//                    log.debug("{} 未安装 {}", device.getSerial(), UI2_DEBUG_PACKAGE);
//                }
//                boolean unicodeIMEInstall = ADBCommandUtils.isInstalledBasepackage(device.getSerial(), UNICODEIME_PACKAGE);
//                if(!unicodeIMEInstall) {
//                    log.debug("{} 未安装 {}", device.getSerial(), UNICODEIME_PACKAGE);
//                }
//                boolean keyboardserviceInstall = ADBCommandUtils.isInstalledBasepackage(device.getSerial(), KEYBOARD_SERVICE_PACKAGE);
//                if(!keyboardserviceInstall) {
//                    log.warn("{} 未安装 {}", device.getSerial(), KEYBOARD_SERVICE_PACKAGE);
//                }
                return ClientInfo.newBuilder()
                        .setDeviceId(device.getSerial())
                        .setBrand(brand)
                        .setCpuabi(cpuabi)
                        .setDensity(density)
                        .setHeight(height)
                        .setWidth(width)
                        .setHost(host)
                        .setModel(model)
                        .setOsName("Android")
                        .setSdk(sdk)
                        .setUserFlag(UserInfo.token)
                        .setVersion(version)
//                        .setSftagentInstall(stfagentInstall)
//                        .setAppiumUiautomator2ServerInstall(appiumServerInstall)
//                        .setAppiumUiautomator2DebugInstall(appiumDebugInstall)
//                        .setKeyboardserviceInstall(keyboardserviceInstall)
//                        .setUnicodeIMEInstall(unicodeIMEInstall)
                        .setRemoteConnectPort(this.socatPort)
                        .setIp(agentInfo.getHost())
                        .setTcpipCommandSuccessed(true)
                        .build();
            }
        } catch (IOException | JadbException e) {
            log.error("[{}] Register device to server error ", this.device.getSerial(), e.getMessage());
        }
        log.error("[{}] device not online ", this.device.getSerial());
        throw new DeviceInitException("设备["+this.device.getSerial()+"不在线，无法初始化");
    }

    private synchronized void initTcpipCommand() {
        String content = ADBTools.isTcpip(device.getSerial()).trim();
        try {
            int intPort = Integer.parseInt(content);
            if(intPort == 0) {
                log.error("[{}] adb tcpip", this.device.getSerial());
                ADBTools.tcpip(device.getSerial(), this.tcpipPort);
            }else{
                this.tcpipPort = intPort;
            }

        }catch (Exception e) {
            return;
        }

    }


    @Override
    public void onNext(Message message) {
        if(Message.Topic.CONNECTED.equals(message.getTopicName())) {
            log.info("[{}] Connected to server", this.device.getSerial());
            // 已经连接
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {

            }
            // 连上服务器之后归零
            connectRetryTime.set(0);
            startStf();
            return;
        }
        IRemoteCommandCallBack call;
        try {
            if(cache.containsKey(message.getTopicName())){
                call = cache.get(message.getTopicName());
            }else{
                call = RemoteCommandCallBackUtils.getCallBack(message.getTopicName(), this);
                cache.put(message.getTopicName(), call);
            }
            call.callback(message.getMessage());
        } catch (Exception e) {
            log.error("["+device.getSerial()+"] command error", e);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        // 出现异常，通常是连接失败
        try {
            log.error("[{}] Register fail", this.device.getSerial(), throwable.getMessage());
            try {
                TimeUnit.MILLISECONDS.sleep(connectRetryTime.get() * 200L);
            } catch (InterruptedException e) {

            }
            deviceInit();
        } catch (DeviceInitException e) {
            log.error("[{}] Register retry error", this.device.getSerial(), throwable.getMessage());
        }
    }

    @Override
    public void onCompleted() {

    }

    public void changeRotation(int rotation) {
        this.userRotation = rotation;
        if(this.screenProjection == null) {
            return;
        }
        if(this.screenProjection.isRunning()) {
            restartProjection(rotation);
        }
    }

    /**
     * @Description: 启动 stfagent
     * @Param: []
     * @Return: void
     * @Author wen
     * @Date 2019-07-02 22:48
     */
    private void startStf(){
        // start server
        this.stfAgentServer = new StfAPKServer(this.device.getSerial(), this.capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.RESOURCE_PATH));
        this.stfAgentServer.start();

        // start service and agent
        this.stfAgentClient = new StfAgentClient(this.device.getSerial());
        this.stfServiceClient = new StfServiceClient(this.device.getSerial(), this);
        this.stfAgentClient.start();
        this.stfServiceClient.start();
    }

    /**
     * @Description: 关闭stf
     * @Param: []
     * @Return: void
     * @Author wen
     * @Date 2019-07-02 22:48
     */
    private void stopStf(){
        // 启动 stfagent
        if(this.stfAgentServer != null) {
            this.stfAgentServer.close();
        }
        if(this.stfAgentClient != null) {
            this.stfAgentClient.close();
        }
        if(this.stfServiceClient != null) {
            this.stfServiceClient.close();
        }
    }
}
