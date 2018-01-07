package com.testwa.distest.server.rpc.service;

import com.alibaba.fastjson.JSON;
import com.testwa.core.WebsocketEvent;
import com.testwa.core.redis.RedisCacheManager;
import com.testwa.distest.common.enums.DB;
import com.testwa.distest.config.security.JwtTokenUtil;
import com.testwa.distest.server.entity.Task;
import com.testwa.distest.server.LogInterceptor;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.mvc.event.GameOverEvent;
import com.testwa.distest.server.rpc.GRpcService;
import com.testwa.distest.server.service.cache.mgr.TaskCacheMgr;
import com.testwa.distest.server.service.task.service.TaskService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.device.auth.DeviceAuthMgr;
import com.testwa.distest.server.web.task.execute.ProcedureRedisMgr;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.task.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * Created by wen on 09/09/2017.
 */
@GRpcService(interceptors = { LogInterceptor.class })
public class TaskGvice extends TaskServiceGrpc.TaskServiceImplBase{
    private static final Logger log = LoggerFactory.getLogger(TaskGvice.class);

    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private TaskCacheMgr taskCacheMgr;
    @Autowired
    private DeviceAuthMgr deviceAuthMgr;
    @Autowired
    ApplicationContext context;
    @Autowired
    private ProcedureRedisMgr procedureRedisMgr;

    @Override
    public void gameover(TaskOverRequest request, StreamObserver<CommonReply> responseObserver) {
        Long exeId = request.getExeId();
        Long timestamp = request.getTimestamp();

        Task exeTask = taskService.findOne(exeId);
        if(exeTask != null){
            if(exeTask.getStatus().getValue() != DB.TaskStatus.CANCEL.getValue()){
                exeTask.setStatus(DB.TaskStatus.COMPLETE);
            }
            exeTask.setEndTime(new Date(timestamp));
            taskService.update(exeTask);
            exeTask.getDevices().forEach(d -> {
                deviceAuthMgr.releaseDev(d.getDeviceId());
            });
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

    @Override
    public void procedureInfoUpload(ProcedureInfoUploadRequest request, StreamObserver<CommonReply> responseObserver) {
        String info = request.getInfoJson();
        procedureRedisMgr.addProcedureToQueue(info);
    }
}
