package com.testwa.distest.client.device.driver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.testwa.core.cmd.AppInfo;
import com.testwa.core.cmd.KeyCode;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.component.appium.utils.Config;
import com.testwa.distest.client.component.executor.task.*;
import com.testwa.distest.client.component.logcat.DLogger;
import com.testwa.distest.client.component.minicap.ScreenIOSProjection;
import com.testwa.distest.client.component.wda.driver.DriverCapabilities;
import com.testwa.distest.client.component.wda.driver.IOSDriver;
import com.testwa.distest.client.device.listener.IOSComponentServiceRunningListener;
import com.testwa.distest.client.device.listener.callback.IRemoteCommandCallBack;
import com.testwa.distest.client.device.listener.callback.RemoteCommandCallBackUtils;
import com.testwa.distest.client.device.manager.DeviceInitException;
import com.testwa.distest.client.device.remote.DeivceRemoteApiClient;
import com.testwa.distest.client.download.Downloader;
import com.testwa.distest.client.exception.DownloadFailException;
import com.testwa.distest.client.ios.IOSDeviceUtil;
import com.testwa.distest.client.ios.IOSPhysicalSize;
import com.testwa.distest.client.model.AgentInfo;
import com.testwa.distest.client.model.UserInfo;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.device.DeviceType;
import io.rpc.testwa.agent.ClientInfo;
import io.rpc.testwa.agent.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * iOS设备驱动
 *
 * @author wen
 * @create 2019-05-23 18:52
 */
@Slf4j
public class IOSRemoteControlDriver implements IDeviceRemoteControlDriver, StreamObserver<Message> {
    private volatile ConcurrentHashMap<Message.Topic, IRemoteCommandCallBack> cache = new ConcurrentHashMap<>();
    private static final String WDA_PROJECT = "WebDriverAgent.xcodeproj";
    private final static DeviceType TYPE = DeviceType.IOS;
    private final String udid;
    private ClientInfo clientInfo;

    private IDeviceRemoteControlDriverCapabilities capabilities;

    private IOSDriver iosDriver;
    private IOSComponentServiceRunningListener listener;
    private DriverCapabilities iosDriverCapabilities;

    private DeivceRemoteApiClient api;
    private ScreenIOSProjection screenIOSProjection;

    /*------------------------------------------LOG----------------------------------------------------*/

    private DLogger dLogger;

    /*------------------------------------------远程任务----------------------------------------------------*/

    private AbstractTestTask task = null;

    private AtomicInteger connectRetryTime = new AtomicInteger(0);

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public IOSRemoteControlDriver(IDeviceRemoteControlDriverCapabilities capabilities) {

        this.capabilities = capabilities;

        this.udid = capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.DEVICE_ID);

        this.api = ApplicationContextUtil.getBean(DeivceRemoteApiClient.class);

        this.listener = new IOSComponentServiceRunningListener(capabilities.getCapability(IDeviceRemoteControlDriverCapabilities.IDeviceKey.DEVICE_ID), api);

        //检查是否能拿到信息
        IOSDeviceUtil.getProductType(udid);
    }

    @Override
    public void deviceInit() throws DeviceInitException {
        clientInfo = buildClientInfo();
        register();
    }

    @Override
    public void register() {
        this.api.registerToServer(clientInfo, this);
    }

    @Override
    public boolean isOnline() {
        return IOSDeviceUtil.isOnline(udid);
    }

    @Override
    public void startProjection(String command) {
        stopProjection();
        this.screenIOSProjection = new ScreenIOSProjection(this.udid, this.listener);
        this.screenIOSProjection.startServer();
    }


    @Override
    public void stopProjection() {
        if(this.screenIOSProjection != null) {
            this.screenIOSProjection.close();
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
        return this.udid;
    }

    @Override
    public void destory() {
        // 停止所有服务
        this.stopProjection();
        this.stopLog();
        this.stopRecorder();
        if(this.iosDriver != null) {
            this.iosDriver.quit();
        }
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
        if(this.iosDriver != null) {
            this.iosDriver.input(cmd);
        }
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
        String distestApiWeb = Config.getString("cloud.web.url");

        String appUrl = String.format("%s/app/%s", distestApiWeb, appInfo.getPath());
        String appLocalPath = Constant.localAppPath + File.separator + appInfo.getMd5() + File.separator + appInfo.getFileName();

        executorService.submit(() -> {
            // 检查是否有和该app md5一致的
            try {
                Downloader d = new Downloader();
                d.start(appUrl, appLocalPath);
                log.info("[{}] install app {}", udid, appLocalPath);

                if(iosDriver != null) {
                    iosDriver.installApp(appLocalPath);
//                this.iosDriver.launch(appInfo.getPackageName());
                }
            } catch (DownloadFailException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void uninstallApp(String bundleId) {
        executorService.submit(() -> {
            if(this.iosDriver != null) {
                this.iosDriver.terminate(bundleId);
                this.iosDriver.removeApp(bundleId);
            }
        });

    }

    @Override
    public void openWeb(String cmd) {
        if(this.iosDriver != null) {
//            this.iosDriver.openWeb(cmd);
        }
    }

    @Override
    public void runShell(String cmd) {
        // TODO
    }

    @Override
    public void capture() {

    }

    @Override
    public void information() {

    }

    @Override
    public void startCrawlerTask(String command) {
        log.info("[{}] 开始遍历任务 {}", udid, command);
        RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);
//        defaultVideoRecorderStart(cmd.getTaskCode());
        task = new CrawlerTestTask(cmd, listener);
        TaskDispatcher.getInstance().submit(task);
    }

    @Override
    public void startCompatibilityTask(String command) {
        log.info("[{} 开始兼容任务 {}", udid, command);
        RemoteRunCommand cmd = JSON.parseObject(command, RemoteRunCommand.class);
//        defaultVideoRecorderStart(cmd.getTaskCode());
        task = new CompatibilityAndroidTestTask(cmd, listener);
        TaskDispatcher.getInstance().submit(task);
    }

    @Override
    public void startFunctionalTask(String command) {
        log.info("[{}] 开始回归测试任务 {}", udid, command);
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
        log.info("[{}] Register device to server, agentInfo: {}", udid, agentInfo.toString());
        String cpu = IOSDeviceUtil.getCPUArchitecture(udid);
        String model = IOSDeviceUtil.getModel(udid);
        String productVersion = IOSDeviceUtil.getProductVersion(udid);
        IOSPhysicalSize size = IOSDeviceUtil.getSize(udid);
        if(size == null) {
            throw new DeviceInitException("设备[" + udid + "]初始化IOSPhysicalSize失败");
        }
        String width = String.valueOf(size.getPhsicalWidth());
        String height = String.valueOf(size.getPhsicalHeight());

        String wdaHome = Config.getString("wda.home");
        Path wdaProject = Paths.get(wdaHome, WDA_PROJECT);
        double scale = (size.getPointWidth() * 1.0)/size.getPhsicalWidth();
        this.iosDriverCapabilities= new DriverCapabilities();
        this.iosDriverCapabilities.setDeviceId(udid);
        this.iosDriverCapabilities.setWdaPath(wdaProject.toString());
        this.iosDriverCapabilities.setSale(String.valueOf(scale));
        this.iosDriverCapabilities.setLaunchTimeout(30);


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

    @Override
    public void onNext(Message message) {
        if(Message.Topic.CONNECTED.equals(message.getTopicName())) {
            log.info("[{}] Connected to Server", this.udid);
            // 连上服务器之后归零
            connectRetryTime.set(0);
            // 连接上服务器之后启动wda
            this.iosDriver = new IOSDriver(this.iosDriverCapabilities);
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
            log.error("回调错误", e);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        // 出现异常
        log.warn("[{}] register error {}", udid, throwable);
        try {
            try {
                TimeUnit.MILLISECONDS.sleep(connectRetryTime.get() * 200L);
            } catch (InterruptedException e) {

            }
            deviceInit();
        } catch (DeviceInitException e) {
            log.error("设备重新注册失败");
        }
    }

    @Override
    public void onCompleted() {

    }
}
