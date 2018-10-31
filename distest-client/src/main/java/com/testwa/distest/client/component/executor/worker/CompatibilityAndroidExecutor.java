package com.testwa.distest.client.component.executor.worker;import com.testwa.core.cmd.RemoteRunCommand;import com.testwa.distest.client.android.ADBCommandUtils;import com.testwa.distest.client.component.executor.ExecutorLog;import com.testwa.distest.client.component.executor.TestTaskListener;import com.testwa.distest.client.exception.*;import com.testwa.distest.client.model.UserInfo;import io.rpc.testwa.task.ExecutorAction;import io.rpc.testwa.task.StepRequest;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import java.io.IOException;import java.nio.file.*;/** * @Program: distest * @Description: 兼容android测试 * @Author: wen * @Create: 2018-05-15 14:38 **/@Slf4jpublic class CompatibilityAndroidExecutor extends CompatibilityAbstractExecutor {    public void init(RemoteRunCommand cmd, TestTaskListener listener) {        super.init(cmd, listener);    }    public void start() {        try {            downloadApp();            loggerStart();            installApp();            launch();            run();            uninstallApp();            complete();        }catch (InstallAppException | LaunchAppException | UninstallAppException | TestcaseRunningException e){            // 安装失败            String error = e.getMessage();            if(StringUtils.isBlank(error)) {                error = "没有错误信息";            }            log.error("设备 {} 执行任务 {} 失败， {} ", device.getName(), cmd.getTaskCode(), error);            grpcClientService.gameover(cmd.getTaskCode(), deviceId, error);        } catch (Exception e) {            // 未知异常            String error = e.getMessage();            if(StringUtils.isBlank(error)) {                error = "未知错误信息";            }            log.error("设备 {} 执行任务 {} 失败， {} ", device.getName(), cmd.getTaskCode(), error, e);            grpcClientService.gameover(cmd.getTaskCode(), deviceId, error);        } finally {            cleanThread();        }    }    @ExecutorLog(action = ExecutorAction.downloadApp)    public void downloadApp(){        super.downloadApp();    }    @ExecutorLog(action = ExecutorAction.installApp)    public void installApp() throws InstallAppException {        super.installApp();    }    @ExecutorLog(action = ExecutorAction.launch)    public void launch() throws LaunchAppException {        super.launch();    }    @ExecutorLog(action = ExecutorAction.run)    @Override    public void run() throws TestcaseRunningException {        // 为了避免干扰测试流程，比如 appium 会使用到 uiautomator，关闭 uiserver//        ui2ServerStop();        // 开始测试        startRecodPerformance();        String screen = ADBCommandUtils.monkey(deviceId, androidApp.getBasePackage(), 1);        stopRecodPerformance();        // 上传截图        log.info("截图路径：{}", screen);        Path screenPath = Paths.get(screen);        try {            // TODO 检查是否有crash日志            Files.walk(screenPath, 2, FileVisitOption.values())                .filter(path -> path.toString().endsWith("png"))                .forEach( img -> {                    if(img.toFile().isFile()){//                    String imgRelativePath = img.toString().replace(Constant.AGENT_TMP_DIR, "");//                    String dumpRelativePath = imgRelativePath.replace("png", "xml");                        String imgName = img.getFileName().toString();                        String dumpName = imgName.replace("png", "xml");                        // TODO 需要压缩图片                        StepRequest request = StepRequest.newBuilder()                                .setToken(UserInfo.token)                                .setTaskCode(cmd.getTaskCode())                                .setDeviceId(deviceId)                                .setDump(dumpName)                                .setImg(imgName)                                .setAction(StepRequest.StepAction.operation)                                .setStatus(StepRequest.StepStatus.SUCCESS)                                .setRuntime(200)                                .build();                        grpcClientService.saveStep(request);                    }            });            grpcClientService.saveImgDir(screen, cmd.getTaskCode(), deviceId);        } catch (IOException e) {            log.error("截图文件无法读取");        }    }    @ExecutorLog(action = ExecutorAction.uninstallApp)    public void uninstallApp() throws UninstallAppException {        super.uninstallApp();    }    @ExecutorLog(action = ExecutorAction.stop)    @Override    public void stop() {        loggerStop();        ui2ServerStop();        StepRequest request = StepRequest.newBuilder()                .setToken(UserInfo.token)                .setTaskCode(cmd.getTaskCode())                .setDeviceId(deviceId)                .setAction(StepRequest.StepAction.stop)                .setStatus(StepRequest.StepStatus.SUCCESS)                .build();        grpcClientService.saveStep(request);    }    @ExecutorLog(action = ExecutorAction.complete)    public void complete() {        super.complete();    }}