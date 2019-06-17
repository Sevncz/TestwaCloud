package com.testwa.distest.client.device.driver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.ddmlib.IDevice;
import com.testwa.core.cmd.AppInfo;
import com.testwa.core.cmd.RemoteRunCommand;
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
import com.testwa.distest.client.device.listener.AndroidComponentServiceRunningListener;
import com.testwa.distest.client.device.listener.IDeviceRemoteCommandListener;
import com.testwa.distest.client.device.remote.DeivceRemoteApiClient;
import com.testwa.distest.client.download.Downloader;
import com.testwa.distest.client.exception.DeviceNotReadyException;
import com.testwa.distest.client.exception.DownloadFailException;
import com.testwa.distest.client.model.AgentInfo;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.jadb.JadbDevice;
import com.testwa.distest.jadb.JadbException;
import io.rpc.testwa.device.DeviceType;
import io.rpc.testwa.push.ClientInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Android设备驱动
 *
 * @author wen
 * @create 2019-05-23 18:52
 */
@Slf4j
public class AndroidRemoteControlDriver implements IDeviceRemoteControlDriver {

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


    /*------------------------------------------LOGCAT----------------------------------------------------*/

    private DLogger dLogger;


    /*------------------------------------------远程DEBUG----------------------------------------------------*/

    private final int tcpipPort;
    private final int socatPort;
    private AndroidDebugServer androidDebugServer;
    private boolean enabledTcpip = false;
    private Long tcpipTime;
    private IDeviceRemoteCommandListener commandListener;
    /**
     * 毫秒
     * 超过该时间再断开，即算断开
     */
    private static final Long TCPIP_TIMEOUT = 20*1000L;
    /*------------------------------------------远程任务----------------------------------------------------*/

    private AbstractTestTask task = null;


    public AndroidRemoteControlDriver(IDeviceRemoteControlDriverCapabilities capabilities){
        this.capabilities = capabilities;

        this.api = new DeivceRemoteApiClient(capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.HOST), Integer.parseInt(capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.PORT)));

        this.device = JadbDeviceManager.getJadbDevice(capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.DEVICE_ID));

        this.listener = new AndroidComponentServiceRunningListener(capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.DEVICE_ID), api);

        this.tcpipPort = TcpIpPortProvider.pullPort();

        this.socatPort = SocatPortProvider.pullPort();
    }

    @Override
    public void deviceInit() {
        try {
            clientInfo = buildClientInfo();
            initTcpipCommand();
            this.commandListener = new IDeviceRemoteCommandListener(device.getSerial(), this);
            register();
        } catch (DeviceNotReadyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void register() {
        this.api.registerToServer(clientInfo, commandListener);
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
    public void startScreen(String command) {
        // 获取请求的配置
        JSONObject obj = JSON.parseObject(command);
        Float scale = obj.getFloat("scale");
        // 设置缩放
        if (scale == null) {scale = defaultScale;}
        if (scale < 0.01) {scale = 0.01f;}
        if (scale > 1.0) {scale = 1.0f;}
        // 设置旋转
        Integer rotate = obj.getInteger("rotate");
        if (rotate == null) {
            rotate = 0;
        }
        stopScreen();

        if(this.screenProjection == null) {
            this.screenProjection = new ScreenAndroidProjection(this.device.getSerial(), this.capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.RESOURCE_PATH), this.listener);
            this.screenProjection.setZoom(scale);
            this.screenProjection.setRotate(rotate);
            this.screenProjection.setQuality(QUALITY);
            this.screenProjection.start();
        }else{
            this.screenProjection.start();
        }
        if(this.touchProjection == null) {
            this.touchProjection = new TouchAndroidProjection(this.device.getSerial(), this.capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.RESOURCE_PATH));
            this.touchProjection.start();
        }else{
            this.touchProjection.start();
        }


    }

    @Override
    public void waitScreen() {
        this.listener.setScreenWait(true);
    }

    @Override
    public void notifyScreen() {
        this.listener.setScreenWait(false);
    }

    @Override
    public void stopScreen() {
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
    public boolean isRealOffline() {
        // 检查是不是tcpip命令已执行成功
        if(this.enabledTcpip){
            // 检查是否超时
            Long currentTime = System.currentTimeMillis();
            if((currentTime - tcpipTime) > TCPIP_TIMEOUT) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void debugStart() {
        ADBTools.forward(device.getSerial(), this.tcpipPort, this.tcpipPort);
        if(this.androidDebugServer == null) {
            this.androidDebugServer = new AndroidDebugServer(this.tcpipPort, this.socatPort);
        }
        if(!this.androidDebugServer.isRunning()) {
            this.androidDebugServer.start();
        }
    }

    @Override
    public void debugStop() {
        if(this.androidDebugServer.isRunning()) {
            this.androidDebugServer.stop();
        }
    }

    @Override
    public void keyevent(int keyCode) {
        if (this.touchProjection != null && this.touchProjection.isRunning()) {
            this.touchProjection.sendCode(keyCode);
        }
    }

    @Override
    public void inputText(String cmd) {
        if (this.touchProjection != null && this.touchProjection.isRunning()) {
            this.touchProjection.sendText(cmd);
        }
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
    public void installApp(String command) {

        AppInfo appInfo = JSON.parseObject(command, AppInfo.class);
        String distestApiWeb = Config.getString("distest.api.web");

        String appUrl = String.format("http://%s/app/%s", distestApiWeb, appInfo.getPath());
        String appLocalPath = Constant.localAppPath + File.separator + appInfo.getMd5() + File.separator + appInfo.getFileName();

        // 检查是否有和该app md5一致的
        try {
            Downloader d = new Downloader();
            d.start(appUrl, appLocalPath);
        } catch (DownloadFailException | IOException e) {
            e.printStackTrace();
        }
        log.info("设备 {} 开始下载及安装App", device.getSerial());

        ADBCommandUtils.installApp(device.getSerial(), appLocalPath);
        ADBCommandUtils.launcherApp(device.getSerial(), appLocalPath);
    }

    @Override
    public void uninstallApp(String url) {
        ADBTools.uninstallApp(device.getSerial(), url);
    }

    @Override
    public void openWeb(String cmd) {
        ADBTools.openWeb(this.device.getSerial(), cmd);
    }

    @Override
    public void startCrawlerTask(String command) {
        log.info("设备 {}，开始遍历任务 {}", device.getSerial(), command);
        RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);
//        defaultVideoRecorderStart(cmd.getTaskCode());
        task = new CrawlerTestTask(cmd, listener);
        TaskDispatcher.getInstance().submit(task);
    }

    @Override
    public void startCompatibilityTask(String command) {
        log.info("设备 {}，开始兼容任务 {}", device.getSerial(), command);
        RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);
//        defaultVideoRecorderStart(cmd.getTaskCode());
        task = new CompatibilityAndroidTestTask(cmd, listener);
        TaskDispatcher.getInstance().submit(task);
    }

    @Override
    public void startFunctionalTask(String command) {
        log.info("设备 {}，开始任务 {}", device.getSerial(), command);
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

    private ClientInfo buildClientInfo() throws DeviceNotReadyException {
        AgentInfo agentInfo = AgentInfo.getAgentInfo();
        log.info("buildClientInfo - deviceId: {} agentInfo: {}", this.device.getSerial(), agentInfo.toString());
        IDevice dev = AndroidHelper.getInstance().getAndroidDevice(this.device.getSerial()).getDevice();
        if(dev != null && dev.isOnline()){
            String brand = dev.getProperty("ro.product.brand");
            if(StringUtils.isBlank(brand)){
                if(buildClientInfoMaxTime <= 0){
                    throw new DeviceNotReadyException("无法获取设备属性");
                }
                buildClientInfoMaxTime--;
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return buildClientInfo();
            }
            String cpuabi = dev.getProperty(IDevice.PROP_DEVICE_CPU_ABI);
            String sdk = dev.getProperty(IDevice.PROP_BUILD_API_LEVEL);
            String host = dev.getProperty("ro.build.host");
            String model = dev.getProperty(IDevice.PROP_DEVICE_MODEL);
            String version = dev.getProperty(IDevice.PROP_BUILD_VERSION);
            String density = dev.getDensity() + "";
            PhysicalSize size = ADBCommandUtils.getPhysicalSize(dev.getSerialNumber());
            // 设置默认scale
            if(size.getWidth() >= BASE_WIDTH) {
                double rate = BASE_WIDTH / (size.getWidth()*1.0);
                this.defaultScale = BigDecimal.valueOf(rate).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
            }
            String width = String.valueOf(size.getWidth());
            String height = String.valueOf(size.getHeight());


            // 检查 uiautomator2、stfagent 等组件的安装情况
            boolean stfagentInstall = ADBCommandUtils.isInstalledBasepackage(dev.getSerialNumber(), STF_SERVICE_PACKAGE);
            if(!stfagentInstall) {
                log.warn("{} 未安装 {}", dev.getName(), STF_SERVICE_PACKAGE);
            }
            boolean appiumServerInstall = ADBCommandUtils.isInstalledBasepackage(dev.getSerialNumber(), UI2_SERVER_PACKAGE);
            if(!appiumServerInstall) {
                log.warn("{} 未安装 {}", dev.getName(), UI2_SERVER_PACKAGE);
            }
            boolean appiumDebugInstall = ADBCommandUtils.isInstalledBasepackage(dev.getSerialNumber(), UI2_DEBUG_PACKAGE);
            if(!appiumDebugInstall) {
                log.debug("{} 未安装 {}", dev.getName(), UI2_DEBUG_PACKAGE);
            }
            boolean unicodeIMEInstall = ADBCommandUtils.isInstalledBasepackage(dev.getSerialNumber(), UNICODEIME_PACKAGE);
            if(!unicodeIMEInstall) {
                log.debug("{} 未安装 {}", dev.getName(), UNICODEIME_PACKAGE);
            }
            boolean keyboardserviceInstall = ADBCommandUtils.isInstalledBasepackage(dev.getSerialNumber(), KEYBOARD_SERVICE_PACKAGE);
            if(!keyboardserviceInstall) {
                log.warn("{} 未安装 {}", dev.getName(), KEYBOARD_SERVICE_PACKAGE);
            }
            return ClientInfo.newBuilder()
                    .setDeviceId(dev.getSerialNumber())
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
                    .setSftagentInstall(stfagentInstall)
                    .setAppiumUiautomator2ServerInstall(appiumServerInstall)
                    .setAppiumUiautomator2DebugInstall(appiumDebugInstall)
                    .setKeyboardserviceInstall(keyboardserviceInstall)
                    .setUnicodeIMEInstall(unicodeIMEInstall)
                    .setRemoteConnectPort(this.socatPort)
                    .setIp(agentInfo.getHost())
                    .setTcpipCommandSuccessed(this.enabledTcpip)
                    .build();
        }else{
            throw new DeviceNotReadyException("设备不在线，无法初始化");
        }

    }

    private void initTcpipCommand() {
        try {
            if(device != null) {
                device.enableAdbOverTCP(this.tcpipPort);
                this.tcpipTime = System.currentTimeMillis();
                this.enabledTcpip = true;
            }
        } catch (IOException | JadbException e) {
            log.error("tcpip 命令执行失败", e);
        }
    }


}
