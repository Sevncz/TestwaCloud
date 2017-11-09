package com.testwa.distest.server.rpc.service;

import com.testwa.distest.common.enums.DB;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.LogInterceptor;
import com.testwa.distest.server.mvc.event.GameOverEvent;
import com.testwa.distest.server.rpc.GRpcService;
import com.testwa.distest.server.service.cache.mgr.TaskCacheMgr;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.web.auth.jwt.JwtTokenUtil;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.task.CommonReply;
import io.rpc.testwa.task.CurrentExeInfoRequest;
import io.rpc.testwa.task.TaskOverRequest;
import io.rpc.testwa.task.TaskServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.Objects;

/**
 * Created by wen on 09/09/2017.
 */
@GRpcService(interceptors = { LogInterceptor.class })
public class TaskGvice extends TaskServiceGrpc.TaskServiceImplBase{
    private static final Logger log = LoggerFactory.getLogger(TaskGvice.class);

    @Autowired
    private TaskService executionTaskService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private TaskCacheMgr taskCacheMgr;
    @Autowired
    ApplicationContext context;

    @Override
    public void gameover(TaskOverRequest request, StreamObserver<CommonReply> responseObserver) {
        String token = request.getToken();

        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        if(userId == null){
            log.error("task's token error, userId is null, token ==== {}", token);
            return;
        }
        Long exeId = request.getExeId();
        Long timestamp = request.getTimestamp();

        Task exeTask = executionTaskService.findOne(exeId);
        if(exeTask != null && Objects.equals(exeTask.getCreateBy(), userId)){
            if(exeTask.getStatus().getValue() != DB.TaskStatus.CANCEL.getValue()){
                exeTask.setStatus(DB.TaskStatus.COMPLETE);
            }
            exeTask.setEndTime(new Date(timestamp));
            executionTaskService.save(exeTask);
            context.publishEvent(new GameOverEvent(this, request.getExeId()));
        }else{
            log.error("exeTask info not format. {}", request.toString());
        }
    }

    @Override
    public void currExeInfo(CurrentExeInfoRequest request, StreamObserver<CommonReply> responseObserver){

        Long exeId = request.getExeId();
        String deviceId = request.getDeviceId();
        Long scriptId = request.getScriptId();
        Long testcaseId = request.getTestcaseId();

        taskCacheMgr.saveExeInfo(exeId, deviceId, testcaseId, scriptId);

    }

}
