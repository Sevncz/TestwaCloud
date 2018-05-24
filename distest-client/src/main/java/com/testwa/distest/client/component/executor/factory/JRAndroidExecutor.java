package com.testwa.distest.client.component.executor.factory;import com.github.cosysoft.device.android.AndroidDevice;import com.github.cosysoft.device.android.impl.AndroidDeviceStore;import com.github.cosysoft.device.android.impl.DefaultAndroidApp;import com.github.cosysoft.device.shell.AndroidSdk;import com.testwa.core.cmd.AppInfo;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.core.shell.UTF8CommonExecs;import com.testwa.distest.client.ApplicationContextUtil;import com.testwa.distest.client.component.ADBCommandUtils;import com.testwa.distest.client.component.Constant;import com.testwa.distest.client.component.FlowResult;import com.testwa.distest.client.component.StepResult;import com.testwa.distest.client.component.appium.utils.Config;import com.testwa.distest.client.component.executor.ExecutorActionInfo;import com.testwa.distest.client.download.Downloader;import com.testwa.distest.client.exception.DownloadFailException;import com.testwa.distest.client.exception.InstallAppException;import com.testwa.distest.client.exception.LaunchAppException;import com.testwa.distest.client.exception.UninstallAppException;import com.testwa.distest.client.model.UserInfo;import com.testwa.distest.client.service.GrpcClientService;import io.rpc.testwa.task.StepRequest;import lombok.extern.slf4j.Slf4j;import org.apache.commons.exec.CommandLine;import org.apache.commons.lang3.StringUtils;import java.io.File;import java.io.IOException;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.concurrent.Executors;import java.util.concurrent.ScheduledExecutorService;import java.util.concurrent.ScheduledFuture;import java.util.concurrent.TimeUnit;import java.util.zip.ZipEntry;import java.util.zip.ZipOutputStream;/** * @Program: distest * @Description: 兼容android测试 * @Author: wen * @Create: 2018-05-15 14:38 **/@Slf4jpublic class JRAndroidExecutor extends JRAbstractExecutor {    private RemoteRunCommand cmd;    private String distestApiWeb;    private Downloader downloader;    private AppInfo appInfo;    private String deviceId;    private String appLocalPath;    private AndroidDevice device;    private DefaultAndroidApp androidApp;    private UTF8CommonExecs exec;    private GrpcClientService grpcClientService;    private Path logcatTempFile;    private ScheduledExecutorService scheduledExecutor;    private String pid;    private FlowResult startFlow;    private Integer cpukel;    private Integer lastFps;    private boolean isClickComplete = false;    @ExecutorActionInfo(desc = "参数初始化", order = 0)    public void init(RemoteRunCommand cmd) {        this.grpcClientService = (GrpcClientService) ApplicationContextUtil.getBean("grpcClientService");        this.cmd = cmd;        this.distestApiWeb = Config.getString("distest.api.web");        this.appInfo = cmd.getAppInfo();        this.deviceId = cmd.getDeviceId();        this.downloader = new Downloader();        this.device = AndroidDeviceStore.getInstance().getDeviceBySerial(deviceId);        this.scheduledExecutor = Executors.newScheduledThreadPool(2);        this.logcatTempFile = Paths.get(Constant.localLogcatPath, Thread.currentThread().getId() + ".txt");        this.cpukel = ADBCommandUtils.getCpuKel(deviceId);        try {            log.info("logcat 临时文件: {}", logcatTempFile.toString());            Files.deleteIfExists(logcatTempFile);            Files.createFile(logcatTempFile);        } catch (IOException e) {        }    }    @ExecutorActionInfo(desc = "下载APP", order = 1)    @Override    public void downloadApp(){        String appUrl = String.format("http://%s/app/%s", distestApiWeb, appInfo.getPath());        this.appLocalPath = Constant.localAppPath + File.separator + appInfo.getMd5() + File.separator + appInfo.getFileAliasName();        // 检查是否有和该app md5一致的        StepResult result = new StepResult(StepRequest.StepAction.downloadApp);        Long start = System.currentTimeMillis();        try {            downloader.start(appUrl, appLocalPath);        } catch (DownloadFailException | IOException e) {            result.setStatus(StepRequest.StepStatus.FAIL);            result.setErrormsg(e.getMessage());        }finally {            Long end = System.currentTimeMillis();            result.setTotalTime(end - start);        }        sendStepRequest(result);    }    @ExecutorActionInfo(desc = "任务队列初始化", order = 2)    public void start() {        int initialDelay = 0;        int period = 1;        try {            androidApp = new DefaultAndroidApp(new File(appLocalPath));            logger();            installApp();            launch();            // 性能抓取任务参数初始化            scheduledExecutor.scheduleWithFixedDelay(getPerformanceTask, initialDelay, period, TimeUnit.SECONDS);            monkeyClick();            uninstallApp();            complete();        }catch (InstallAppException e){            // 安装失败        } catch (LaunchAppException e) {            // 启动失败        } catch (UninstallAppException e) {        } finally {            loggerStop();        }    }    private Runnable getPerformanceTask = new Runnable() {        @Override        public void run() {            if(!isClickComplete){                try {                    if(StringUtils.isBlank(pid)){                        pid = ADBCommandUtils.getPid(deviceId, androidApp.getBasePackage());                    }                    if(startFlow == null){                        startFlow = ADBCommandUtils.getFlow(deviceId, pid);                    }                    Double cpu = null;                    if(cpukel == null) {                        log.error("设备 {} 获取cpu指标错误，cpu数量为空", deviceId);                        cpukel = ADBCommandUtils.getCpuKel(deviceId);                    }                    if(cpukel != null) {                        cpu = ADBCommandUtils.cpuRate(deviceId, pid, cpukel);                    }                    // 获得累计流量                    FlowResult[] flows = ADBCommandUtils.flow(deviceId, pid, startFlow); // kb                    startFlow = flows[0];                    FlowResult resultFlow = flows[1];                    Integer mem = ADBCommandUtils.mem(deviceId, androidApp.getBasePackage()); // kb                    Integer bat = ADBCommandUtils.battery(deviceId);                    Integer fps = ADBCommandUtils.fps(deviceId, androidApp.getBasePackage());                    if(fps == null){                        if (lastFps != null){                            fps = lastFps;                        }else{                            fps = 0;                        }                    }else{                        lastFps = fps;                    }                    log.info("mem: {}, bat: {}, cpu: {}, fps: {} flow: {}", mem, bat, cpu, fps, resultFlow);                    grpcClientService.savePreformance(cpu, mem, bat, fps, resultFlow, cmd.getExeId(), deviceId);                } catch (Exception e) {                    log.error("设备 {} 获取指标异常", deviceId, e);                }            }        }    };    @ExecutorActionInfo(desc = "安装", order = 3)    @Override    public void installApp() throws InstallAppException {        if(ADBCommandUtils.isInstalledBasepackage(deviceId, androidApp.getBasePackage())){            ADBCommandUtils.uninstallApp(deviceId, androidApp.getBasePackage());        }        StepResult result = ADBCommandUtils.installApp(deviceId, appLocalPath, 300*1000L);        sendStepRequest(result);        if(StepRequest.StepStatus.FAIL.equals(result.getStatus())){            throw new InstallAppException(result.getErrormsg());        }    }    private void sendStepRequest(StepResult result) {        StepRequest request = StepRequest.newBuilder()                .setToken(UserInfo.token)                .setTaskId(cmd.getExeId())                .setDeviceId(deviceId)                .setAction(result.getAction())                .setStatus(result.getStatus())                .setRuntime(result.getTotalTime())                .build();        grpcClientService.saveStep(request);    }    @ExecutorActionInfo(desc = "启动", order = 4)    @Override    public void launch() throws LaunchAppException {        StepResult result = ADBCommandUtils.launcherApp(deviceId, appLocalPath);        sendStepRequest(result);        if(StepRequest.StepStatus.FAIL.equals(result.getStatus())){            throw new LaunchAppException(result.getErrormsg());        }    }    @ExecutorActionInfo(desc = "点击", order = 5)    @Override    public void monkeyClick() {        String screen = ADBCommandUtils.monkey(deviceId, androidApp.getBasePackage(), 2);        isClickComplete = true;        // 上传截图        log.info("截图路径：{}", screen);        Path screenPath = Paths.get(screen);        try {            Files.list(screenPath)                .filter(path -> path.toString().endsWith("png"))                .forEach( img -> {//                    String imgRelativePath = img.toString().replace(Constant.AGENT_TMP_DIR, "");//                    String dumpRelativePath = imgRelativePath.replace("png", "xml");                    String imgName = img.getFileName().toString();                    String dumpName = imgName.replace("png", "xml");                    StepRequest request = StepRequest.newBuilder()                            .setToken(UserInfo.token)                            .setTaskId(cmd.getExeId())                            .setDeviceId(deviceId)                            .setDump(dumpName)                            .setImg(imgName)                            .setAction(StepRequest.StepAction.operation)                            .setStatus(StepRequest.StepStatus.SUCCESS)                            .setRuntime(200)                            .build();                    grpcClientService.saveStep(request);            });            // 压缩，上传//            String zipFilePath = screenPath.getParent().toString() + ".zip";//            pack(screenPath.toString(), zipFilePath);//            grpcClientService.zipFileUpload(Paths.get(zipFilePath), cmd.getExeId(), deviceId);            grpcClientService.saveImgDir(screen, cmd.getExeId(), deviceId);        } catch (IOException e) {            log.error("截图文件无法读取");        }    }    private void pack(String sourceDirPath, String zipFilePath) throws IOException {        Path p = Files.createFile(Paths.get(zipFilePath));        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {            Path pp = Paths.get(sourceDirPath);            Files.walk(pp)                    .filter(path -> !Files.isDirectory(path))                    .forEach(path -> {                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());                        try {                            zs.putNextEntry(zipEntry);                            Files.copy(path, zs);                            zs.closeEntry();                        } catch (IOException e) {                            System.err.println(e);                        }                    });        }    }    @ExecutorActionInfo(desc = "卸载", order = 6)    @Override    public void uninstallApp() throws UninstallAppException {        StepResult result = ADBCommandUtils.uninstallApp(deviceId, androidApp.getBasePackage());        sendStepRequest(result);        if(StepRequest.StepStatus.FAIL.equals(result.getStatus())){            throw new UninstallAppException(result.getErrormsg());        }    }    @Override    public void stop() {        if(this.exec != null){            this.exec.destroy();        }        StepRequest request = StepRequest.newBuilder()                .setToken(UserInfo.token)                .setTaskId(cmd.getExeId())                .setDeviceId(deviceId)                .setAction(StepRequest.StepAction.stop)                .setStatus(StepRequest.StepStatus.SUCCESS)                .build();        grpcClientService.saveStep(request);    }    @ExecutorActionInfo(desc = "完成", order = 7)    @Override    public void complete() {        this.grpcClientService.logcatFileUpload(logcatTempFile, cmd.getExeId(), deviceId);        StepRequest request = StepRequest.newBuilder()                .setToken(UserInfo.token)                .setTaskId(cmd.getExeId())                .setDeviceId(deviceId)                .setAction(StepRequest.StepAction.complete)                .setStatus(StepRequest.StepStatus.SUCCESS)                .build();        grpcClientService.saveStep(request);    }    /**     *@Description: 启动logcat，并记录     *@Param: []     *@Return: void     *@Author: wen     *@Date: 2018/5/14     */    @Override    public void logger() {        loggerClear();        CommandLine commandLine = new CommandLine(AndroidSdk.adb());        commandLine.addArgument("-s");        commandLine.addArgument(this.deviceId);        commandLine.addArgument("logcat");        commandLine.addArgument("-v");        commandLine.addArgument("time");        commandLine.addArgument("process");        commandLine.addArgument("*:W");        exec = new UTF8CommonExecs(commandLine);        // 设置最大执行时间，10分钟        exec.setTimeout(10*60*1000L);        try {            exec.asyncexec();            // 启动任务            int initialDelay = 0;            int period = 10;            scheduledExecutor.scheduleWithFixedDelay(saveLogcatToFileTask, initialDelay, period, TimeUnit.SECONDS);        } catch (IOException e) {            e.printStackTrace();        }    }    private Runnable saveLogcatToFileTask = () -> exec.outputToFile(logcatTempFile);    private void loggerClear() {        CommandLine commandLine = new CommandLine(AndroidSdk.adb());        commandLine.addArgument("-s");        commandLine.addArgument(this.deviceId);        commandLine.addArgument("-c");        UTF8CommonExecs clexec = new UTF8CommonExecs(commandLine);        try {            clexec.exec();        } catch (IOException e) {        }    }    private void loggerStop() {        if(this.exec != null){            this.exec.destroy();        }        if(this.scheduledExecutor != null){            this.scheduledExecutor.shutdown();        }    }}