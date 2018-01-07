package com.testwa.distest.client.control.event;

import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.client.control.client.task.Executor;
import com.testwa.distest.client.control.client.task.pool.ExecutorPool;
import com.testwa.distest.client.grpc.GrpcClient;
import com.testwa.distest.client.grpc.Gvice;
import com.testwa.distest.client.model.UserInfo;
import io.grpc.Channel;
import io.rpc.testwa.task.CurrentExeInfoRequest;
import io.rpc.testwa.task.TaskOverRequest;
import io.rpc.testwa.task.TaskServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wen on 19/08/2017.
 */
@Component
public class TestcaseRunListener implements ApplicationListener<TestcaseRunEvent> {
    private static Logger log = LoggerFactory.getLogger(TestcaseRunListener.class);
    private Map<String, Executor> excutors = new HashMap<>();

    @Autowired
    public ExecutorPool pool;
    @GrpcClient("local-grpc-server")
    private Channel serverChannel;

    @Async
    @Override
    public void onApplicationEvent(TestcaseRunEvent testcaseRunEvent) {
        log.info("run cmd ...");
        RemoteRunCommand cmd = testcaseRunEvent.getCmd();

        switch (cmd.getCmd()){
            case 0:
                // 停止
                Executor executor1 = excutors.get(cmd.getDeviceId());
                if(executor1 != null){
                    executor1.pythonStop();
                    pool.release(executor1);
                }
                break;
            case 1:
                // 启动
                if(excutors.size() >= 5){
                    break;
                }
                Executor executor3 = excutors.get(cmd.getDeviceId());
                if(executor3 != null){
                    log.error("this device {} was running", cmd.getDeviceId());
                    break;
                }
                Executor executor2 = pool.getService();
                try {
                    excutors.put(cmd.getDeviceId(), executor2);
                    executor2.setChannel(serverChannel);
                    executor2.setAppId(cmd.getAppId());
                    executor2.setDeviceId(cmd.getDeviceId());
                    executor2.setInstall(cmd.getInstall());
                    executor2.setTaskId(cmd.getExeId());
                    executor2.setTestcaseList(cmd.getTestcaseList());
                    executor2.runScripts();
                }catch (Exception e){
                    log.error("excutors error", e);
                }finally {

                    pool.release(executor2);
                    excutors.remove(cmd.getDeviceId());

                    // 测试结束，通知服务器结束
                    TaskOverRequest taskOverRequest = TaskOverRequest.newBuilder()
                            .setToken(UserInfo.token)
                            .setExeId(cmd.getExeId())
                            .setTimestamp(TimeUtil.getTimestampLong())
                            .build();

                    Gvice.taskService(serverChannel).gameover(taskOverRequest);
                }
                log.info("excutors over!");

                break;
            case 2:
                // 检查
                Executor executor4 = excutors.get(cmd.getDeviceId());
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
                    TaskServiceGrpc.newFutureStub(serverChannel).currExeInfo(request);
                }
                break;
        }

    }

}
