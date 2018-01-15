package com.testwa.distest.client.event;

import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.appium.AppiumManager;
import com.testwa.distest.client.exception.AppiumStartFailedException;
import com.testwa.distest.client.executor.*;
import com.testwa.distest.client.appium.pool.AppiumManagerPool;
import com.testwa.distest.client.exception.DownloadFailException;
import com.testwa.distest.client.service.GrpcClientService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wen on 19/08/2017.
 */
@Log4j2
@Component
public class TestcaseRunListener implements ApplicationListener<TestcaseRunEvent> {
    private ConcurrentHashMap<String, PythonExecutor> executors = new ConcurrentHashMap<>();

    @Autowired
    public AppiumManagerPool pool;
    @Autowired
    public GrpcClientService grpcClientService;
    @Value("${agent.web.url}")
    private String agentWebUrl;
    @Autowired
    private ApplicationContext context;

    @Async
    @Override
    public void onApplicationEvent(TestcaseRunEvent testcaseRunEvent) {
        log.info("Event: run cmd {}", testcaseRunEvent.getCmd().toString());
        RemoteRunCommand cmd = testcaseRunEvent.getCmd();

        switch (cmd.getCmd()){
            case 0:
                // 停止
                PythonExecutor executor1 = executors.get(cmd.getDeviceId());
                if(executor1 != null){
                    executor1.stop();
                }
                break;
            case 1:
                log.info("Task start on device {}", cmd.getDeviceId());

                // 启动
                if(executors.size() >= 10){
                    log.error("executors size over 10");
                    break;
                }
                PythonExecutor executor3 = executors.get(cmd.getDeviceId());
                if(executor3 != null){
                    log.error("this device {} was running", cmd.getDeviceId());
                    break;
                }
                final AppiumManager manager = pool.getManager();
                String appiumLogPath = manager.getAppiumlogPath();
                try {
                    String appiumUrl = manager.getAppiumService().getUrl().toString();
                    PythonExecutor executor2 = new PythonExecutor(agentWebUrl, appiumUrl);
                    executors.put(cmd.getDeviceId(), executor2);
                    executor2.setAppId(cmd.getAppId());
                    executor2.setDeviceId(cmd.getDeviceId());
                    executor2.setInstall(cmd.getInstall());
                    executor2.setTaskId(cmd.getExeId());
                    executor2.setTestcaseList(cmd.getTestcaseList());

                    AbstractExecutorHandler appHander = new AppDownloadExecutorHandler();
                    AbstractExecutorHandler scriptHander = new ScriptDownloadExecutorHandler();
                    AbstractExecutorHandler pythonHander = new PythonExecutorHandler();
                    // 如A处理不掉转交给B
                    appHander.setHandler(scriptHander);
                    scriptHander.setHandler(pythonHander);
                    appHander.handleRequest(executor2);
                }catch (DownloadFailException | IOException  e){
                    log.error("executors error", e);
                }finally {
                    //upload log
                    sendLogsToServer(UploadFileToServerEvent.FileType.APPIUMLOG, cmd.getExeId(), cmd.getDeviceId(), appiumLogPath);
//                        sendLogsToServer(UploadFileToServerEvent.FileType.LOGCAT, cmd.getExeId(), cmd.getDeviceId());
                }
                pool.release(manager);
                // 通知结束
                executors.remove(cmd.getDeviceId());
                grpcClientService.gameover(cmd.getExeId());
                log.info("executors over!");
                break;
            case 2:
                // 检查
                PythonExecutor executor4 = executors.get(cmd.getDeviceId());
                if(executor4 != null){
                    Long currScriptId = executor4.getCurrScript();
                    Long currTestcaseId = executor4.getCurrTestCaseId();
                    grpcClientService.notifyServerCurrentTaskExecutorInfo(cmd.getDeviceId(), cmd.getExeId(), currScriptId, currTestcaseId);
                }
                break;
        }

    }

    private void sendLogsToServer(UploadFileToServerEvent.FileType fileType, Long taskId, String deviceId, String filePath) {
        context.publishEvent(new UploadFileToServerEvent(this, taskId, deviceId,  fileType, filePath));
    }

}
