package com.testwa.distest.client.control.client.task;

import com.testwa.core.model.RemoteRunCommand;
import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.client.control.client.task.pool.ExecutorPool;
import io.grpc.ManagedChannel;
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
    @Autowired
    private ManagedChannel managedChannel;

    @Async
    @Override
    public void onApplicationEvent(TestcaseRunEvent testcaseRunEvent) {
        log.info("run cmd ...");
        RemoteRunCommand cmd = testcaseRunEvent.getCmd();

        switch (cmd.getCmd()){
            case 0:
                Executor executor1 = excutors.get(cmd.getDeviceId());
                if(executor1 != null){
                    executor1.pythonStop();
                    pool.release(executor1);
                }
                break;
            case 1:
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
                            .setExeId(cmd.getExeId())
                            .setTimestamp(TimeUtil.getTimestampLong())
                            .build();

                    TaskServiceGrpc.newFutureStub(managedChannel).gameover(taskOverRequest);
                }
                log.info("excutors over!");

                break;
        }

    }

}
