package com.testwa.distest.server.api.grpc;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.distest.server.LogInterceptor;
import com.testwa.distest.server.api.WSFlagEnum;
import com.testwa.distest.server.grpc.GRpcService;
import com.testwa.distest.server.service.redis.TestwaDeviceRedisService;
import io.grpc.stub.StreamObserver;
import io.grpc.testwa.device.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

/**
 * Created by wen on 2017/06/09.
 */
@GRpcService(interceptors = { LogInterceptor.class })
public class ControlService extends ControlGrpc.ControlImplBase{
    private static final Logger log = LoggerFactory.getLogger(ControlService.class);

    @Autowired
    private SocketIOServer server;

    @Override
    public void forward(CommandRequest request, StreamObserver<CommonReply> responseObserver) {



    }
}
