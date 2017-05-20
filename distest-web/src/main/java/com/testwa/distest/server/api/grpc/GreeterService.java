package com.testwa.distest.server.api.grpc;

import com.testwa.distest.server.LogInterceptor;
import com.testwa.distest.server.grpc.GRpcService;
import io.grpc.stub.StreamObserver;
import io.grpc.testwa.helloworld.GreeterGrpc;
import io.grpc.testwa.helloworld.HelloReply;
import io.grpc.testwa.helloworld.HelloRequest;

/**
 * Created by wen on 2016/11/27.
 */
@GRpcService(interceptors = { LogInterceptor.class })
public class GreeterService extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        final HelloReply.Builder replyBuilder = HelloReply.newBuilder().setMessage("Hello " + request.getName());
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }

}
