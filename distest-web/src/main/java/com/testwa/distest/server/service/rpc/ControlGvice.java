package com.testwa.distest.server.service.rpc;

import com.corundumstudio.socketio.SocketIOServer;
import io.grpc.stub.StreamObserver;
import io.rpc.testwa.device.*;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by wen on 2017/06/09.
 */
@Slf4j
@GRpcService
public class ControlGvice extends ControlServiceGrpc.ControlServiceImplBase{
    @Autowired
    private SocketIOServer server;

    @Override
    public void forward(CommandRequest request, StreamObserver<CommonReply> responseObserver) {

    }
}
