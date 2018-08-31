package com.testwa.distest.client.component.executor.worker;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.core.shell.UTF8CommonExecs;import com.testwa.distest.client.android.ADBCommandUtils;import com.testwa.distest.client.component.Constant;import com.testwa.distest.client.component.appium.utils.Config;import com.testwa.distest.client.component.executor.ExecutorLog;import com.testwa.distest.client.component.executor.TestTaskListener;import com.testwa.distest.client.component.executor.uiautomator2.Ui2ServerForAppium;import com.testwa.distest.client.exception.InstallAppException;import com.testwa.distest.client.exception.LaunchAppException;import com.testwa.distest.client.exception.UninstallAppException;import com.testwa.distest.client.model.UserInfo;import io.rpc.testwa.task.ExecutorAction;import io.rpc.testwa.task.StepRequest;import lombok.extern.slf4j.Slf4j;import org.apache.commons.exec.CommandLine;import org.apache.commons.lang3.StringUtils;import java.io.File;import java.io.IOException;import java.nio.file.FileVisitOption;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.concurrent.ExecutorService;import java.util.concurrent.Executors;import java.util.concurrent.TimeUnit;import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;/** * @Program: distest * @Description: 遍历测试执行器 * @Author: wen * @Create: 2018-07-19 18:35 **/@Slf4jpublic class CrawlerExecutor extends CrawlerAbstractExecutor{    private int appiumPort;    private Long taskCode;    private Thread ui2Thread;    private boolean isChecking = true;    private Ui2ServerForAppium ui2ServerForAppium;    private UTF8CommonExecs javaExecs;    private boolean hasError = false;    private StringBuffer errorMsg = new StringBuffer();    private String resourcePath;    private String cralwerJarPath;    private String cralwerYamlPath;    private static final String cralwer = "UICrawler-2.0.jar";    @Override    public void init(int appiumPort, RemoteRunCommand cmd, TestTaskListener listener) {        this.resourcePath = Config.getString("distest.agent.resources");        this.cralwerJarPath = Paths.get(resourcePath, "crawler", cralwer).toString();        this.cralwerYamlPath = Paths.get(resourcePath, "crawler", "config.yml").toString();        this.appiumPort = appiumPort;        this.taskCode = cmd.getTaskCode();        super.init(cmd, listener);    }    @ExecutorLog(action = ExecutorAction.downloadApp)    public void downloadApp() {        super.downloadApp();    }    @Override    public void start() {        try {            loggerStart();            installApp();            launch(); // 这里启动是为了点掉启动之后的权限弹框            run();            uninstallApp();            complete();        }catch (InstallAppException e){//             安装失败            String error = e.getMessage();            if(StringUtils.isBlank(error)) {                error = "没有错误信息";            }            log.error("【遍历测试】设备 {} 执行任务 {} 失败， {} ", device.getName(), cmd.getTaskCode(), error);            grpcClientService.gameover(taskCode, deviceId, error);        } catch (Exception e) {            // 未知错误            String error = e.getMessage();            if(StringUtils.isBlank(error)) {                error = "未知错误信息";            }            log.error("【遍历测试】设备 {} 执行任务 {} 失败， {} ", device.getName(), cmd.getTaskCode(), error);            grpcClientService.gameover(taskCode, deviceId, error);        } finally {            loggerStop();            ui2ServerStop();        }    }    @ExecutorLog(action = ExecutorAction.installApp)    public void installApp() throws InstallAppException {        super.installApp();    }    @ExecutorLog(action = ExecutorAction.launch)    public void launch() throws LaunchAppException {        super.launch();    }    @ExecutorLog(action = ExecutorAction.run)    @Override    public void run() {        StepRequest request = null;        try {            // 检查ui2是否启动，如果挂掉，则进行重启            ui2Thread = new Thread(new Runnable() {                boolean isStrat = false;                @Override                public void run() {                    while(isChecking) {                        boolean isExsit = false;                        String ps_output = ADBCommandUtils.command(deviceId, new String[]{"ps"});                        String[] outs = ps_output.split("\n");                        for(String line : outs) {                            if(line.contains("io.appium.uiautomator2.server")) {                                isStrat = true;                                isExsit = true;                            }                        }                        if(isStrat) {                            if(!isExsit) {                                if( ui2ServerForAppium == null) {                                    // 拉起uiautomator2                                    ui2ServerForAppium = new Ui2ServerForAppium(deviceId);                                    ui2ServerForAppium.start();                                    try {                                        TimeUnit.SECONDS.sleep(10);                                    } catch (InterruptedException e) {                                        e.printStackTrace();                                    }                                }                            }                        }                    }                }            });            ui2Thread.start();            String reportOut = Constant.localCrawlerOutPath + File.separator + deviceId + File.separator + taskCode;            String screenOut = Constant.localCrawlerOutPath + File.separator + deviceId + File.separator + taskCode + File.separator + "screenshot";            CommandLine commandLine = new CommandLine("java");            commandLine.addArgument("-jar");            commandLine.addArgument(cralwerJarPath);            commandLine.addArgument("-u");            commandLine.addArgument(cmd.getDeviceId());            commandLine.addArgument("-t");            commandLine.addArgument(String.valueOf(appiumPort));            commandLine.addArgument("-f");            commandLine.addArgument(cralwerYamlPath);            commandLine.addArgument("-a");            commandLine.addArgument(cmd.getAppInfo().getActivity());            commandLine.addArgument("-p");            commandLine.addArgument(cmd.getAppInfo().getPackageName());            commandLine.addArgument("-o");            commandLine.addArgument(reportOut);            javaExecs = new UTF8CommonExecs(commandLine);            try {                javaExecs.setTimeout(INFINITE_TIMEOUT);                javaExecs.exec();                String output = javaExecs.getOutput();            } catch (IOException e) {                String error = javaExecs.getError();                log.error("Crawler执行错误 \n {}", error, e);                hasError = true;                errorMsg.append(error).append("\n");            }            Path screenPath = Paths.get(screenOut);            try {                Files.walk(screenPath, 2, FileVisitOption.values())                        .filter(path -> path.toString().endsWith("png"))                        .forEach( img -> {                            if(img.toFile().isFile()){                                String imgName = img.getFileName().toString();                                StepRequest stepRequest = StepRequest.newBuilder()                                        .setToken(UserInfo.token)                                        .setTaskCode(cmd.getTaskCode())                                        .setDeviceId(deviceId)                                        .setImg(imgName)                                        .setAction(StepRequest.StepAction.operation)                                        .setStatus(StepRequest.StepStatus.SUCCESS)                                        .setRuntime(200)                                        .build();                                grpcClientService.saveStep(stepRequest);                            }                        });                grpcClientService.saveImgDir(screenOut, cmd.getTaskCode(), deviceId);            } catch (IOException e) {                log.error("截图文件无法读取");            }//            uploadReport(Paths.get(reportOut));        } catch (Exception e) {            log.error("【遍历测试】{} 执行失败", device.getName(), e);            request = StepRequest.newBuilder()                    .setToken(UserInfo.token)                    .setTaskCode(cmd.getTaskCode())                    .setDeviceId(deviceId)                    .setAction(StepRequest.StepAction.operation)                    .setStatus(StepRequest.StepStatus.ERROR)                    .setRuntime(200)                    .build();        }finally {            log.info("【遍历测试】{} 完成", device.getName());            if(request != null) {                grpcClientService.saveStep(request);            }            this.ui2Thread.interrupt();            this.isChecking = false;            ui2ServerForAppium.close();        }    }    @ExecutorLog(action = ExecutorAction.uninstallApp)    public void uninstallApp() throws UninstallAppException {        super.uninstallApp();    }    @Override    public void stop() {        if(javaExecs != null) {            javaExecs.destroy();        }    }    @ExecutorLog(action = ExecutorAction.complete)    public void complete() {        super.complete();    }    private void uploadReport(Path resultDir) {        log.info("crawler report path: {}", resultDir.toString());        try {            Files.walk(resultDir, 2, FileVisitOption.values()).forEach(i -> {                if(i.toFile().isFile()){                    grpcClientService.fileUpload(i, resultDir.toString(), "crawler", taskCode, deviceId);                }            });        } catch (IOException e) {            e.printStackTrace();        }    }}