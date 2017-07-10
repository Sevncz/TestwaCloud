package com.testwa.distest.server.api.grpc;

import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.distest.server.LogInterceptor;
import com.testwa.distest.server.grpc.GRpcService;
import io.grpc.stub.StreamObserver;
import io.grpc.testwa.device.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by wen on 2017/06/09.
 */
@GRpcService(interceptors = { LogInterceptor.class })
public class ControlGvice extends ControlServiceGrpc.ControlServiceImplBase{
    private static final Logger log = LoggerFactory.getLogger(ControlGvice.class);

    @Autowired
    private SocketIOServer server;

    @Override
    public void forward(CommandRequest request, StreamObserver<CommonReply> responseObserver) {

    }
}
