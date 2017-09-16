package com.testwa.distest.server.rpc.service;

import com.testwa.core.utils.TimeUtil;
import com.testwa.distest.server.LogInterceptor;
import com.testwa.distest.server.mvc.event.GameOverEvent;
import com.testwa.distest.server.mvc.model.ExecutionTask;
import com.testwa.distest.server.mvc.model.Task;
import com.testwa.distest.server.mvc.service.TaskService;
import com.testwa.distest.server.mvc.service.cache.RemoteClientService;
import com.testwa.distest.server.rpc.GRpcService;
import com.testwa.distest.server.security.JwtTokenUtil;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.task.CommonReply;
import io.rpc.testwa.task.CurrentExeInfoRequest;
import io.rpc.testwa.task.TaskOverRequest;
import io.rpc.testwa.task.TaskServiceGrpc;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.List;

/**
 * Created by wen on 09/09/2017.
 */
@GRpcService(interceptors = { LogInterceptor.class })
public class TaskGvice extends TaskServiceGrpc.TaskServiceImplBase{
    private static final Logger log = LoggerFactory.getLogger(TaskGvice.class);

    @Autowired
    private TaskService taskService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private RemoteClientService remoteClientService;
    @Autowired
    ApplicationContext context;

    @Override
    public void gameover(TaskOverRequest request, StreamObserver<CommonReply> responseObserver) {
        String token = request.getToken();

        String userId = jwtTokenUtil.getUserIdFromToken(token);
        if(StringUtils.isBlank(userId)){
            log.error("task's token error, userId is null, token ==== {}", token);
            return;
        }
        String exeId = request.getExeId();
        Long timestamp = request.getTimestamp();

        ExecutionTask exeTask = taskService.getExeTaskById(exeId);
        if(exeTask != null && exeTask.getCreator().equals(userId)){
            if(exeTask.getStatus() != ExecutionTask.StatusEnum.CANCEL.getCode()){
                exeTask.setStatus(ExecutionTask.StatusEnum.STOP.getCode());
            }
            exeTask.setEndTime(new Date(timestamp));
            taskService.saveExetask(exeTask);
            context.publishEvent(new GameOverEvent(this, exeId));
        }else{
            log.error("exeTask info not format. {}", request.toString());
        }
    }

    @Override
    public void currExeInfo(CurrentExeInfoRequest request, StreamObserver<CommonReply> responseObserver){

        String exeId = request.getExeId();
        String deviceId = request.getDeviceId();
        String scriptId = request.getScriptId();
        String testcaseId = request.getTestcaseId();

        remoteClientService.saveExeInfo(exeId, deviceId, testcaseId, scriptId);

    }

}
