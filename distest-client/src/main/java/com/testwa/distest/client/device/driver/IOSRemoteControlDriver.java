package com.testwa.distest.client.device.driver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.testwa.core.cmd.AppInfo;
import com.testwa.core.cmd.KeyCode;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.distest.client.component.appium.utils.Config;
import com.testwa.distest.client.component.executor.task.*;
import com.testwa.distest.client.component.logcat.DLogger;
import com.testwa.distest.client.component.minicap.ScreenIOSProjection;
import com.testwa.distest.client.component.wda.driver.DriverCapabilities;
import com.testwa.distest.client.component.wda.driver.IOSDriver;
import com.testwa.distest.client.device.listener.IDeviceRemoteCommandListener;
import com.testwa.distest.client.device.listener.IOSComponentServiceRunningListener;
import com.testwa.distest.client.device.remote.DeivceRemoteApiClient;
import com.testwa.distest.client.exception.CommandFailureException;
import com.testwa.distest.client.ios.IOSDeviceUtil;
import com.testwa.distest.client.ios.IOSPhysicalSize;
import com.testwa.distest.client.model.AgentInfo;
import com.testwa.distest.client.model.UserInfo;
import io.rpc.testwa.device.DeviceType;
import io.rpc.testwa.push.ClientInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * iOS设备驱动
 *
 * @author wen
 * @create 2019-05-23 18:52
 */
@Slf4j
public class IOSRemoteControlDriver implements IDeviceRemoteControlDriver {
    private static final String WDA_PROJECT = "WebDriverAgent.xcodeproj";
    private final static DeviceType TYPE = DeviceType.IOS;
    private final String udid;
    private ClientInfo clientInfo;

    private IDeviceRemoteControlDriverCapabilities capabilities;

    private IOSDriver iosDriver;
    private IOSComponentServiceRunningListener listener;
    private DriverCapabilities iosDriverCapabilities;

    private DeivceRemoteApiClient api;
    private IDeviceRemoteCommandListener commandListener;
    private ScreenIOSProjection screenIOSProjection;

    /*------------------------------------------LOG----------------------------------------------------*/

    private DLogger dLogger;

    /*------------------------------------------远程任务----------------------------------------------------*/

    private AbstractTestTask task = null;

    public IOSRemoteControlDriver(IDeviceRemoteControlDriverCapabilities capabilities) {

        this.capabilities = capabilities;

        this.udid = capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.DEVICE_ID);

        this.api = new DeivceRemoteApiClient(capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.HOST), Integer.parseInt(capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.PORT)));

        this.listener = new IOSComponentServiceRunningListener(capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.DEVICE_ID), api);

        //检查是否能拿到信息
        IOSDeviceUtil.getProductType(udid);
    }

    @Override
    public void deviceInit() throws Exception {

        clientInfo = buildClientInfo();
        this.commandListener = new IDeviceRemoteCommandListener(udid, this);
        register();
    }

    @Override
    public void register() {
        this.api.registerToServer(clientInfo, commandListener);
    }

    @Override
    public boolean isOnline() {
        return IOSDeviceUtil.isOnline(udid);
    }

    @Override
    public void startScreen(String command) {
        stopScreen();

        if(this.screenIOSProjection != null && this.screenIOSProjection.isRunning()){
            this.screenIOSProjection.start();
        }else{
            this.screenIOSProjection = new ScreenIOSProjection(udid, this.capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.RESOURCE_PATH), this.listener);
            this.screenIOSProjection.start();
        }

        this.iosDriver = new IOSDriver(this.iosDriverCapabilities);

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

        if(this.screenIOSProjection != null) {
            this.screenIOSProjection.close();
        }
        if(this.iosDriver != null) {
            this.iosDriver.quit();
        }
    }

    @Override
    public void rate(String cmd) {
        JSONObject rateCommand = JSON.parseObject(cmd);
        Integer rate = rateCommand.getInteger("rate");
        this.listener.rate(rate);
    }

    @Override
    public void startLog(String command) {
        // 启动 log 日志服务
        if(dLogger == null) {
            dLogger = new DLogger(this.udid, DeviceType.IOS, this.listener);
            dLogger.start();
        }else{
            if(!dLogger.isRunning()) {
                dLogger.close();
                dLogger = new DLogger(this.udid, DeviceType.IOS, this.listener);
                dLogger.start();
            }
        }
        this.listener.setLogWait(false);
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
        this.listener.setLogWait(true);
        if(dLogger != null) {
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
        return null;
    }

    @Override
    public boolean isRealOffline() {
        return false;
    }

    @Override
    public void debugStart() {
        log.error("iOS no debug start");
    }

    @Override
    public void debugStop() {
        log.error("iOS no debug stop");
    }

    @Override
    public void keyevent(int keyCode) {
        if(this.iosDriver != null) {
            if (keyCode == KeyCode.KEYCODE_HOME){
                this.iosDriver.home();
            }
        }
    }

    @Override
    public void inputText(String cmd) {

    }

    @Override
    public void touch(String cmd) {
        log.error("Android event {}", cmd);
    }

    @Override
    public void swip(String cmd) {
        if(this.iosDriver != null) {
            JSONObject tapCommand = JSON.parseObject(cmd);
            Integer fromX = tapCommand.getInteger("fromX");
            Integer fromY = tapCommand.getInteger("fromY");
            Integer toX = tapCommand.getInteger("toX");
            Integer toY = tapCommand.getInteger("toY");
            Integer duration = tapCommand.getInteger("duration");
            this.iosDriver.swipe(fromX, fromY, toX, toY, duration);
        }
    }

    @Override
    public void tap(String cmd) {
        if(this.iosDriver != null) {
            JSONObject tapCommand = JSON.parseObject(cmd);
            Integer x = tapCommand.getInteger("x");
            Integer y = tapCommand.getInteger("y");
            this.iosDriver.tap(x, y);
        }

    }

    @Override
    public void tapAndHold(String cmd) {

    }

    @Override
    public void installApp(String command) {
        AppInfo appInfo = JSON.parseObject(command, AppInfo.class);
        if(this.iosDriver != null) {
            this.iosDriver.installApp(appInfo.getPath());
        }
    }

    @Override
    public void uninstallApp(String bundleId) {
        if(this.iosDriver != null) {
            this.iosDriver.removeApp(bundleId);
        }

    }

    @Override
    public void openWeb(String cmd) {
        if(this.iosDriver != null) {
//            this.iosDriver.openWeb(cmd);
        }
    }

    @Override
    public void startCrawlerTask(String command) {
        log.info("设备 {}，开始遍历任务 {}", udid, command);
        RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);
//        defaultVideoRecorderStart(cmd.getTaskCode());
        task = new CrawlerTestTask(cmd, listener);
        TaskDispatcher.getInstance().submit(task);
    }

    @Override
    public void startCompatibilityTask(String command) {
        log.info("设备 {}，开始兼容任务 {}", udid, command);
        RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);
//        defaultVideoRecorderStart(cmd.getTaskCode());
        task = new CompatibilityAndroidTestTask(cmd, listener);
        TaskDispatcher.getInstance().submit(task);
    }

    @Override
    public void startFunctionalTask(String command) {
        log.info("设备 {}，开始任务 {}", udid, command);
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

    private ClientInfo buildClientInfo() throws Exception {
        AgentInfo agentInfo = AgentInfo.getAgentInfo();
        log.info("buildClientInfo - uuid: {} agentInfo: {}", udid, agentInfo.toString());
        String cpu = IOSDeviceUtil.getCPUArchitecture(udid);
        String model = IOSDeviceUtil.getModel(udid);
        String productVersion = IOSDeviceUtil.getProductVersion(udid);
        IOSPhysicalSize size = IOSDeviceUtil.getSize(udid);
        String width = String.valueOf(size.getPhsicalWidth());
        String height = String.valueOf(size.getPhsicalHeight());

        String wdaHome = Config.getString("wda.home");
        Path wdaProject = Paths.get(wdaHome, WDA_PROJECT);
        double scale = (size.getPointWidth() * 1.0)/size.getPhsicalWidth();
        this.iosDriverCapabilities= new DriverCapabilities();
        this.iosDriverCapabilities.setDeviceId(udid);
        this.iosDriverCapabilities.setWdaPath(wdaProject.toString());
        this.iosDriverCapabilities.setSale(String.valueOf(scale));
        while(StringUtils.isEmpty(UserInfo.token)){
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {

            }
        }

        return ClientInfo.newBuilder()
                .setDeviceId(udid)
                .setBrand("Apple")
                .setCpuabi(cpu)
                .setModel(model)
                .setOsName("iOS")
                .setUserFlag(UserInfo.token)
                .setVersion(productVersion)
                .setWidth(width)
                .setHeight(height)
                .setIp(agentInfo.getHost())
                .setRemoteConnectPort(8555)
                .setTcpipCommandSuccessed(true)
                .build();
    }
}
