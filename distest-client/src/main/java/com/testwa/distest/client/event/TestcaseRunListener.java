package com.testwa.distest.client.event;

import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.distest.client.component.appium.AppiumManager;
import com.testwa.distest.client.component.executor.*;
import com.testwa.distest.client.component.appium.pool.AppiumManagerPool;
import com.testwa.distest.client.exception.DownloadFailException;
import com.testwa.distest.client.service.GrpcClientService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class TestcaseRunListener implements ApplicationListener<TestcaseRunEvent> {
    private ConcurrentHashMap<String, PythonExecutor> executors = new ConcurrentHashMap<>();

    @Autowired
    public AppiumManagerPool pool;
    @Autowired
    public GrpcClientService grpcClientService;
    @Value("${cloud.web.url}")
    private String agentWebUrl;
    @Value("${distest.api.web}")
    private String distestApiWeb;
    @Value("${distest.api.name}")
    private String distestApiName;
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
                    PythonExecutor executor2 = ProxyFactory.getPyExecutorInstance(PythonExecutor.class, cmd);
                    executor2.init(distestApiWeb, appiumUrl, cmd);
                    executors.put(cmd.getDeviceId(), executor2);

                    AppDownloadExecutorHandler appHander = new AppDownloadExecutorHandler();
                    ScriptDownloadExecutorHandler scriptHander = new ScriptDownloadExecutorHandler();
                    PythonExecutorHandler pythonHander = new PythonExecutorHandler();

                    appHander.setHandler(scriptHander);
                    scriptHander.setHandler(pythonHander);
                    appHander.handleRequest(executor2);

                    grpcClientService.missionComplete(cmd.getExeId(), cmd.getDeviceId());
                } catch (DownloadFailException | IOException  e){
                    log.error("executors error", e);
                    grpcClientService.gameover(cmd.getExeId(), cmd.getDeviceId(), e.getMessage());
                } finally {
                    //upload log
                    grpcClientService.appiumLogUpload(cmd.getExeId(), cmd.getDeviceId(), appiumLogPath);
//                        sendLogsToServer(UploadFileToServerEvent.FileType.LOGCAT, cmd.getExeId(), cmd.getDeviceId());
                }
                pool.release(manager);
                // 通知结束
                executors.remove(cmd.getDeviceId());
                log.info("executors over!");
                break;
            case 2:
                // 检查
                PythonExecutor executor4 = executors.get(cmd.getDeviceId());
                if(executor4 != null){
                    Long currScriptId = executor4.getCurrScript().getId();
                    Long currTestcaseId = executor4.getCurrTestCaseId();
                    grpcClientService.notifyServerCurrentTaskExecutorInfo(cmd.getDeviceId(), cmd.getExeId(), currScriptId, currTestcaseId);
                }
                break;
        }

    }

}
