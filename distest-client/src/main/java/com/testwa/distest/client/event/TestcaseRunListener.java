package com.testwa.distest.client.event;

import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.client.appium.AppiumManager;
import com.testwa.distest.client.executor.*;
import com.testwa.distest.client.appium.pool.AppiumManagerPool;
import com.testwa.distest.client.exception.DownloadFailException;
import com.testwa.distest.client.grpc.GrpcClient;
import com.testwa.distest.client.grpc.Gvice;
import com.testwa.distest.client.model.UserInfo;
import io.grpc.Channel;
import io.rpc.testwa.task.CurrentExeInfoRequest;
import io.rpc.testwa.task.TaskOverRequest;
import io.rpc.testwa.task.TaskServiceGrpc;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @GrpcClient("local-grpc-server")
    private Channel serverChannel;
    @Value("${agent.web.url}")
    private String agentWebUrl;

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
                    executor1.pythonStop();
                    pool.release(executor1.getAppiumManager());
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
                AppiumManager manager = pool.getManager();
                PythonExecutor executor2 = new PythonExecutor(agentWebUrl, manager);
                try {
                    executors.put(cmd.getDeviceId(), executor2);
                    executor2.setChannel(serverChannel);
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
                }catch (DownloadFailException | IOException e){
                    log.error("executors error", e);
                } finally {
                    pool.release(manager);
                    executors.remove(cmd.getDeviceId());

                    // 测试结束，通知服务器，任务已结束
                    TaskOverRequest taskOverRequest = TaskOverRequest.newBuilder()
                            .setToken(UserInfo.token)
                            .setExeId(cmd.getExeId())
                            .setTimestamp(TimeUtil.getTimestampLong())
                            .build();

                    Gvice.taskService(serverChannel).gameover(taskOverRequest);
                }
                log.info("executors over!");

                break;
            case 2:
                // 检查
                PythonExecutor executor4 = executors.get(cmd.getDeviceId());
                if(executor4 != null){
                    Long currScriptId = executor4.getCurrScript();
                    Long currTestcaseId = executor4.getCurrTestCaseId();
                    CurrentExeInfoRequest request = CurrentExeInfoRequest.newBuilder()
                            .setDeviceId(cmd.getDeviceId())
                            .setExeId(cmd.getExeId())
                            .setScriptId(currScriptId)
                            .setTestcaseId(currTestcaseId)
                            .setToken(UserInfo.token)
                            .build();
                    Gvice.taskService(serverChannel).currExeInfo(request);
                }
                break;
        }

    }

}
