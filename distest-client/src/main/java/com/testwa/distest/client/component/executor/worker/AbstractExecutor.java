package com.testwa.distest.client.component.executor.worker;import com.github.cosysoft.device.android.AndroidDevice;import com.github.cosysoft.device.android.impl.AndroidDeviceStore;import com.github.cosysoft.device.android.impl.DefaultAndroidApp;import com.testwa.core.cmd.AppInfo;import com.testwa.core.cmd.KeyCode;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.core.utils.TimeUtil;import com.testwa.distest.client.ApplicationContextUtil;import com.testwa.distest.client.android.ADBCommandUtils;import com.testwa.distest.client.component.Constant;import com.testwa.distest.client.component.FlowResult;import com.testwa.distest.client.component.StepResult;import com.testwa.distest.client.component.appium.utils.Config;import com.testwa.distest.client.component.executor.TestTaskListener;import com.testwa.distest.client.component.executor.uiautomator2.Ui2Command;import com.testwa.distest.client.component.executor.uiautomator2.Bounds;import com.testwa.distest.client.component.executor.uiautomator2.Ui2Server;import com.testwa.distest.client.component.logcat.DLogger;import com.testwa.distest.client.download.Downloader;import com.testwa.distest.client.exception.*;import com.testwa.distest.client.model.UserInfo;import com.testwa.distest.client.service.GrpcClientService;import io.rpc.testwa.task.FileUploadRequest;import io.rpc.testwa.task.StepRequest;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import java.io.File;import java.io.IOException;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.HashMap;import java.util.Map;import java.util.concurrent.*;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-15 14:28 **/@Slf4jpublic abstract class AbstractExecutor {    public static final String TESTWA_PWD = "testwa123";    protected final Path logcatTempFile = Paths.get(Constant.localLogcatPath, Thread.currentThread().getId() + ".txt");    protected final Path actionScreenTempFile = Paths.get(Constant.localActionScreenPath);    protected Ui2Server ui2Server;    protected DLogger dLogger;    protected GrpcClientService grpcClientService;    protected RemoteRunCommand cmd;    protected String distestApiWeb;    protected AppInfo appInfo;    protected String deviceId;    protected Downloader downloader;    protected Integer cpukel;    protected AndroidDevice device;    protected boolean install;    protected String appLocalPath;    protected DefaultAndroidApp androidApp;    private FlowResult startFlow;    private Integer lastFps;    protected final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(15);    private Ui2Command ui2Command;    private ScheduledFuture performFuture;    private ScheduledFuture runningAlterFuture;    private Map<String, ScheduledFuture> installFutures = new HashMap<>();    private Long installStart = null;    private TestTaskListener listener;    public String screenshoot() {        if(listener != null) {            byte[] frame = listener.takeFrame();            if(frame != null) {                try {                    Path screenPath = Paths.get(actionScreenTempFile.toString(), TimeUtil.getTimestampLong() + ".jpg");                    Files.write(screenPath, frame);                    log.info("{} 保存截图至 {}", device.getName(), screenPath.toString());                    return screenPath.toString();                } catch (IOException e) {                    e.printStackTrace();                }            }        }        return null;    }    /**     * 初始化     */    public void init(RemoteRunCommand cmd, TestTaskListener listener) {        this.grpcClientService = (GrpcClientService) ApplicationContextUtil.getBean("grpcClientService");        this.listener = listener;        this.cmd = cmd;        this.distestApiWeb = Config.getString("distest.api.web");        this.appInfo = cmd.getAppInfo();        this.deviceId = cmd.getDeviceId();        this.downloader = new Downloader();        this.device = AndroidDeviceStore.getInstance().getDeviceBySerial(deviceId);        this.install = cmd.getInstall();        this.cpukel = ADBCommandUtils.getCpuKel(deviceId);        try {            log.info("logcat 临时文件: {}", logcatTempFile.toString());            Files.deleteIfExists(logcatTempFile);            Files.createFile(logcatTempFile);        } catch (IOException e) {        }    }    /**     * 下载app     */    public void downloadApp(){        log.info("{} 开始下载app {}", device.getName(), appInfo.getDisplayName());        String appUrl = String.format("http://%s/app/%s", distestApiWeb, appInfo.getPath());        this.appLocalPath = Constant.localAppPath + File.separator + appInfo.getMd5() + File.separator + appInfo.getFileAliasName();        // 检查是否有和该app md5一致的        StepResult result = new StepResult(StepRequest.StepAction.downloadApp);        Long start = System.currentTimeMillis();        try {            downloader.start(appUrl, appLocalPath);        } catch (DownloadFailException | IOException e) {            result.setStatus(StepRequest.StepStatus.FAIL);            result.setErrormsg(e.getMessage());        }finally {            Long end = System.currentTimeMillis();            result.setTotalTime(end - start);        }        sendStepRequest(result);        androidApp = new DefaultAndroidApp(new File(appLocalPath));    }    /**     * 任务启动入口     */    public abstract void start();    /**     * 安装APP     */    public void installApp() throws InstallAppException {        ui2ServerStart();        ADBCommandUtils.unlockWindow(deviceId);        ADBCommandUtils.inputCode(deviceId, KeyCode.KEYCODE_HOME);        if(ADBCommandUtils.isInstalledBasepackage(deviceId, androidApp.getBasePackage())){            ADBCommandUtils.uninstallApp(deviceId, androidApp.getBasePackage());        }        startInstallCheck();        log.info("{} 开始安装应用", device.getName());        StepResult result = ADBCommandUtils.installApp(deviceId, appLocalPath);        Long installEnd = System.currentTimeMillis();        if(installStart != null) {            result.setTotalTime(installEnd - installStart);        }        sleep(10);        stopInstallCheck();        sendStepRequest(result);        if(StepRequest.StepStatus.FAIL.equals(result.getStatus())){            throw new InstallAppException(result.getErrormsg());        }    }    /**     * 启动APP     */    public void launch() throws LaunchAppException {        log.info("{} 开始启动应用", device.getName());        String imgBefore = screenshoot();        String imgAfter = null;        StepResult result = null;        startProcessRunningAlter();        int time = 60;        while(time > 0) {            result = ADBCommandUtils.launcherApp(deviceId, appLocalPath);            String pid = ADBCommandUtils.getPid(deviceId,appInfo.getPackageName());            log.info("{} 检查应用 {} 是否启动, pid = {}", device.getName(), appInfo.getPackageName(), pid);            if(StringUtils.isNotBlank(pid)) {                // 等2秒启动完成                try {                    TimeUnit.SECONDS.sleep(2);                } catch (InterruptedException e) {                }                break;            }            try {                TimeUnit.SECONDS.sleep(1);            } catch (InterruptedException e) {            }            time--;        }        imgAfter = screenshoot();        if(result != null) {            sendStepRequest(result, imgBefore, imgAfter);            if(StepRequest.StepStatus.FAIL.equals(result.getStatus())){                log.info("启动失败");                throw new LaunchAppException(result.getErrormsg());            }        }        // 暂停10秒，让程序处理一下权限弹框        sleep(10);//        if(className.contains("CompatibilityAndroidExecutor")) {            // 完成之后关闭 ui2server//            this.ui2Server.close();//        }        this.ui2ServerStop();    }    /**     * 操作设备     */    public abstract void run() throws TestcaseRunningException;    /**     * 卸载APP     */    public void uninstallApp() throws UninstallAppException {        log.info("{} 开始卸载应用", device.getName());        StepResult result = ADBCommandUtils.uninstallApp(deviceId, androidApp.getBasePackage());        sendStepRequest(result);        if(StepRequest.StepStatus.FAIL.equals(result.getStatus())){            throw new UninstallAppException(result.getErrormsg());        }    }    /**     * 执行完成     */    public void complete() {        // 上传logcat        this.grpcClientService.logcatFileUpload(logcatTempFile, cmd.getTaskCode(), deviceId);        // 上传完成步骤        StepRequest request = StepRequest.newBuilder()                .setToken(UserInfo.token)                .setTaskCode(cmd.getTaskCode())                .setDeviceId(deviceId)                .setAction(StepRequest.StepAction.complete)                .setStatus(StepRequest.StepStatus.SUCCESS)                .build();        grpcClientService.saveStep(request);        // 上报服务器，任务完成        grpcClientService.missionComplete(cmd.getTaskCode(), cmd.getDeviceId());    }    /**     * @Description: 关闭工作线程，打扫线程     * @Param: []     * @Return: void     * @Author wen     * @Date 2018/9/10 17:38     */    protected void cleanThread() {        listener.taskFinish();        log.info("关闭ffmpeg.....");        loggerStop();        ui2ServerStop();        uploadVideo();    }    protected void uploadVideo() {        // 上传测试录像        String videoOutputFile = listener.getVideoFile();        if(StringUtils.isNotBlank(videoOutputFile)) {            Path videoPath = Paths.get(videoOutputFile);            log.info("上传MP4文件 {}", device.getName());            grpcClientService.largeFileUpload(videoPath, videoPath.getParent().toString(), FileUploadRequest.Type.video, this.cmd.getTaskCode(), this.deviceId);            log.info("上传MP4文件完成 {} ", device.getName());            try {                log.info("删除MP4文件");                Files.deleteIfExists(videoPath);            } catch (IOException e) {                log.warn("删除MP4文件失败", e);            }        }    }    /**     * 停止任务     */    public abstract void stop();    void startRecodPerformance() {        int initialDelay = 0;        int period = 1;        // 性能抓取任务参数初始化        performFuture = scheduledExecutor.scheduleWithFixedDelay(getPerformanceTask, initialDelay, period, TimeUnit.SECONDS);    }    void stopRecodPerformance() {        performFuture.cancel(true);    }    private Runnable getPerformanceTask = new Runnable() {        @Override        public void run() {            try {                String pid = ADBCommandUtils.getPid(deviceId, androidApp.getBasePackage());                if(startFlow == null){                    startFlow = ADBCommandUtils.getFlow(deviceId, pid);                }                Double cpu = null;                if(cpukel == null) {                    log.error("设备 {} 获取cpu指标错误，cpu数量为空", device.getName());                    cpukel = ADBCommandUtils.getCpuKel(deviceId);                }                if(cpukel != null) {                    try {                        cpu = ADBCommandUtils.cpuRate(deviceId, pid, cpukel);                    } catch (Exception e) {                    }finally {                        if(cpu == null) {                            cpu = 0.0;                        }                    }                }                // 获得累计流量                FlowResult[] flows = ADBCommandUtils.flow(deviceId, pid, startFlow); // kb                startFlow = flows[0];                FlowResult resultFlow = flows[1];                Integer mem = ADBCommandUtils.ram(deviceId, androidApp.getBasePackage()); // kb                if(mem == null) {                    mem = 0;                }                Integer bat = ADBCommandUtils.battery(deviceId);                if(bat == null) {                    bat = 0;                }                Integer fps = ADBCommandUtils.fps(deviceId, androidApp.getBasePackage());                if(fps == null){                    if (lastFps != null){                        fps = lastFps;                    }else{                        fps = 0;                    }                }else{                    lastFps = fps;                }                log.debug("ram: {}, bat: {}, cpu: {}, fps: {} flow: {}", mem, bat, cpu, fps, resultFlow);                grpcClientService.savePreformance(cpu, mem, bat, fps, resultFlow, cmd.getTaskCode(), deviceId);            } catch (Exception e) {                log.error("设备 {} 获取指标异常", device.getName(), e);            }        }    };    /**     *@Description: 启动logcat，并记录     *@Param: []     *@Return: void     *@Author: wen     *@Date: 2018/5/14     */    void loggerStart() {        this.dLogger = new DLogger(deviceId);        this.dLogger.setLogFile(logcatTempFile);        this.dLogger.start();    }    void loggerStop() {        if(this.dLogger != null){            this.dLogger.close();        }    }    /**     *@Description: 启动一个uiautomator2     *@Param:     *@Return:     *@Author: wen     *@Date: 2018/6/6     */    void ui2ServerStart() {        if(ui2Server == null) {            ui2Server = new Ui2Server(deviceId);            ui2Server.start();            ui2Command = new Ui2Command(ui2Server.getUiServerPort());        }else{            ui2Server.restart();        }    }    void ui2ServerStop() {        if(this.ui2Server != null){            log.warn("{} Uiautomator2 server stop!", device.getName());            stopProcessRunningAlter();            sleep(5);            ui2Server.close();        }    }    void startInstallCheck() {        if(device.getName().toLowerCase().startsWith("xiaomi") || device.getName().toLowerCase().startsWith("redmi")){            log.warn("处理小米 {}弹框", device.getName());            processXIAOMI();        }        if(device.getName().toLowerCase().startsWith("oppo")){            log.warn("处理OPPO {}弹框", device.getName());            processOPPO();        }        if(device.getName().toLowerCase().startsWith("vivo")){            log.warn("处理VIVO {}弹框", device.getName());            processVIVO();        }        if(device.getName().toLowerCase().startsWith("samsung")){            log.warn("处理三星 {}弹框", device.getName());            processSAMSUNG();        }    }    void stopInstallCheck() {        installFutures.values().forEach(f -> {            if(!f.isCancelled()) {                f.cancel(true);            }        });    }    void processSAMSUNG() {        int initialDelay = 2;        int period = 1;        ScheduledFuture future = scheduledExecutor.scheduleWithFixedDelay(this::clickOK, initialDelay, period, TimeUnit.SECONDS);        installFutures.put("clickOK", future);    }    void processXIAOMI() {        int initialDelay = 2;        int period = 1;        ScheduledFuture future = scheduledExecutor.scheduleWithFixedDelay(this::clickContinueInstall, initialDelay, period, TimeUnit.SECONDS);        installFutures.put("clickContinueInstall", future);    }    void processOPPO() {        int period = 2;        ScheduledFuture inputFuture = scheduledExecutor.scheduleWithFixedDelay(this::oppoPwd, 2, period, TimeUnit.SECONDS);        installFutures.put("oppoPwd", inputFuture);        // 点击继续安装        ScheduledFuture clickContinueInstallFuture = scheduledExecutor.scheduleWithFixedDelay(this::clickContinueInstall, 3, period, TimeUnit.SECONDS);        installFutures.put("clickContinueInstall", clickContinueInstallFuture);        // 点击继续安装        ScheduledFuture clickContinueInstallOppoFuture = scheduledExecutor.scheduleWithFixedDelay(this::clickContinueInstallOppo, 3, period, TimeUnit.SECONDS);        installFutures.put("clickContinueInstallOppo", clickContinueInstallOppoFuture);        // 点击安装旧版本        ScheduledFuture clickContinueInstallOldFuture = scheduledExecutor.scheduleWithFixedDelay(this::clickContinueInstallOld, 3, period, TimeUnit.SECONDS);        installFutures.put("clickContinueInstallOld", clickContinueInstallOldFuture);        // 点击安装        ScheduledFuture clickInstallXYFuture = scheduledExecutor.scheduleWithFixedDelay(this::clickInstallXY, 4, period, TimeUnit.SECONDS);        installFutures.put("clickInstallXY", clickInstallXYFuture);        // 点击安装        ScheduledFuture clickInstallFuture = scheduledExecutor.scheduleWithFixedDelay(this::clickInstall, 4, period, TimeUnit.SECONDS);        installFutures.put("clickInstall", clickInstallFuture);    }    void oppoPwd() {        if(inputPwd()) {            clickInstall();        }    }    protected void processVIVO() {        int period = 2;        ScheduledFuture inputFuture = scheduledExecutor.scheduleWithFixedDelay(this::vivoPwd, 2, period, TimeUnit.SECONDS);        installFutures.put("vivoPwd", inputFuture);        ScheduledFuture clickContinueInstallFuture = scheduledExecutor.scheduleWithFixedDelay(this::clickContinueInstall, 3, period, TimeUnit.SECONDS);        installFutures.put("clickContinueInstall", clickContinueInstallFuture);        ScheduledFuture clickContinueInstallOldFuture = scheduledExecutor.scheduleWithFixedDelay(this::clickContinueInstallOld, 3, period, TimeUnit.SECONDS);        installFutures.put("clickContinueInstallOld", clickContinueInstallOldFuture);        ScheduledFuture clickInstallFuture = scheduledExecutor.scheduleWithFixedDelay(this::clickInstall, 4, period, TimeUnit.SECONDS);        installFutures.put("clickInstall", clickInstallFuture);    }    void vivoPwd() {        if(findUnknowSources()) {            if(inputPwd()) {                clickOK();            }        }    }    private boolean findUnknowSources() {        try {            ui2Command.createSession();            String elementId = ui2Command.findElement("new UiSelector().textContains(\"电脑端未知来源\")");            if(StringUtils.isNotBlank(elementId)) {                return true;            }        } catch (Exception e) {            e.printStackTrace();        }        return false;    }    private boolean inputPwd() {        try {            ui2Command.createSession();            String elementId = ui2Command.findElement("new UiSelector().className(\"android.widget.EditText\")");            if(StringUtils.isNotBlank(elementId)) {                if(ui2Command.inputText(elementId, TESTWA_PWD)) {                    return true;                }            }        } catch (Exception e) {            e.printStackTrace();        }        return false;    }    private boolean clickInstall() {        try {            String elementId = ui2Command.findElement("new UiSelector().text(\"安装\")");            if(StringUtils.isNotBlank(elementId)) {                if(ui2Command.click(elementId)) {                    installStart = System.currentTimeMillis();                    log.info("clickInstall {}", installStart);                    return true;                }            }        } catch (Exception e) {            e.printStackTrace();        }        return false;    }    private boolean clickOK() {        String[] texts = {"确定", "确认"};        return clickByTexts(texts);    }    private boolean clickByTexts(String[] texts) {        try {            ui2Command.createSession();            for(String t : texts) {                String elementId = ui2Command.findElement("new UiSelector().text(\"" + t + "\")");                if(StringUtils.isNotBlank(elementId)) {                    if(ui2Command.click(elementId)) {                        return true;                    }                }            }        } catch (Exception e) {            e.printStackTrace();        }        return false;    }    private boolean clickContinueInstall() {        try {            ui2Command.createSession();            String elementId = ui2Command.findElement("new UiSelector().text(\"继续安装\")");            if(StringUtils.isNotBlank(elementId)) {                if(ui2Command.click(elementId)) {                    installStart = System.currentTimeMillis();                    log.info("clickContinueInstall {}", installStart);                    return true;                }            }        } catch (Exception e) {            e.printStackTrace();        }        return false;    }    /**     *@Description: oppo com.android.packageinstaller:id/btn_continue_install_old     *@Param: []     *@Return: boolean     *@Author: wen     *@Date: 2018/6/11     */    private boolean clickContinueInstallOld() {        try {            ui2Command.createSession();            String elementId = ui2Command.findElement("new UiSelector().text(\"继续安装旧版本\")");            if(StringUtils.isNotBlank(elementId)) {                if(ui2Command.click(elementId)) {                    installStart = System.currentTimeMillis();                    log.info("clickContinueInstallOld {}", installStart);                    return true;                }            }        } catch (Exception e) {            e.printStackTrace();        }        return false;    }    private boolean clickInstallXY() {        try {            ui2Command.createSession();            String elementId = ui2Command.findElement("new UiSelector().text(\"应用权限\")");            if(StringUtils.isNotBlank(elementId)) {                log.warn("点击'安装'按钮!");                String bottomButtonId = ui2Command.findElementById("com.android.packageinstaller:id/bottom_button_layout");                if(StringUtils.isNotBlank(bottomButtonId)) {                    Bounds bounds = ui2Command.rect(bottomButtonId);                    if(bounds != null) {                        int y = bounds.getY();                        int x = bounds.getX();                        int h = bounds.getHight();                        int w = bounds.getWidth();                        if(ui2Command.click((w + x)/2, y+(h/3))) {                            installStart = System.currentTimeMillis();                            log.info("clickInstallXY {}", installStart);                            return true;                        }                    }                }            }        } catch (Exception e) {            e.printStackTrace();        }        return false;    }    private boolean clickContinueInstallOppo() {        try {            ui2Command.createSession();            String elementId = ui2Command.findElement("new UiSelector().text(\"未发现风险\")");            if(StringUtils.isNotBlank(elementId)) {                log.debug("点击'继续安装'按钮!");                String installBtnId = ui2Command.findElementById("com.android.packageinstaller:id/safe_button_layout");                if(StringUtils.isNotBlank(installBtnId)) {                    if(ui2Command.click(installBtnId)) {                        installStart = System.currentTimeMillis();                        log.info("clickContinueInstallOppo {}", installStart);                        return true;                    }                }            }        } catch (Exception e) {            e.printStackTrace();        }        return false;    }    void startProcessRunningAlter() {        int initialDelay = 500;        int period = 300;        log.warn("{} 开始检查启动页面弹框检测", this.device.getName());        runningAlterFuture = scheduledExecutor.scheduleWithFixedDelay(this::processRunningAlter, initialDelay, period, TimeUnit.MILLISECONDS);    }    void stopProcessRunningAlter() {        log.warn("{} 启动页面弹框检测 退出... ... ...", this.device.getName());        if(runningAlterFuture != null) {            runningAlterFuture.cancel(true);        }    }    private void processRunningAlter() {        clickAllowBtn();    }    private void clickAllowBtn() {        String[] texts = {"允许", "始终允许"};        clickByTexts(texts);    }    void sleep(int second) {        try {            TimeUnit.SECONDS.sleep(second);        } catch (InterruptedException e) {        }    }    void sendStepRequest(StepResult result) {        StepRequest request = StepRequest.newBuilder()                .setToken(UserInfo.token)                .setTaskCode(cmd.getTaskCode())                .setDeviceId(deviceId)                .setAction(result.getAction())                .setStatus(result.getStatus())                .setRuntime(result.getTotalTime())                .setErrormsg(result.getErrormsg())                .setTimestamp(System.currentTimeMillis())                .build();        grpcClientService.saveStep(request);    }    void sendStepRequest(StepResult result, String imgBefore, String imgAfter) {        if(StringUtils.isAnyBlank(imgBefore, imgAfter)) {            log.info("启动截图为空: before {} after {}", imgBefore, imgAfter);            // 上传步骤            StepRequest request = StepRequest.newBuilder()                    .setToken(UserInfo.token)                    .setTaskCode(cmd.getTaskCode())                    .setDeviceId(deviceId)                    .setAction(result.getAction())                    .setStatus(result.getStatus())                    .setRuntime(result.getTotalTime())                    .setErrormsg(result.getErrormsg())                    .setTimestamp(System.currentTimeMillis())                    .build();            grpcClientService.saveStep(request);            return;        }        Path imgBeforePath = Paths.get(imgBefore);        Path imgAfterPath = Paths.get(imgAfter);        // 上传截图        grpcClientService.imgUpload(cmd.getTaskCode(), imgBefore, deviceId);        grpcClientService.imgUpload(cmd.getTaskCode(), imgAfter, deviceId);        // 上传步骤        StepRequest request = StepRequest.newBuilder()                .setToken(UserInfo.token)                .setTaskCode(cmd.getTaskCode())                .setDeviceId(deviceId)                .setAction(result.getAction())                .setStatus(result.getStatus())                .setRuntime(result.getTotalTime())                .setErrormsg(result.getErrormsg())                .setTimestamp(System.currentTimeMillis())                .setImgBefore(imgBeforePath.getFileName().toString())                .setImg(imgAfterPath.getFileName().toString())                .build();        grpcClientService.saveStep(request);    }}