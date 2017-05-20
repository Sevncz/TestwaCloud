package com.testwa.distest.client.service;

import io.grpc.ManagedChannel;
import io.grpc.testwa.helloworld.GreeterGrpc;
import io.grpc.testwa.helloworld.HelloReply;
import io.grpc.testwa.helloworld.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by wen on 2016/11/27.
 */
@Service
public class HelloGrpcService {
    private static final Logger LOG = LoggerFactory.getLogger(HelloGrpcService.class);
    @Autowired
    private ManagedChannel mChannel;

    public void sayHello(){
        Long start2 = new Date().getTime();
        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(mChannel);
        HelloRequest message = HelloRequest.newBuilder().setName("wen").build();
        HelloReply reply = stub.sayHello(message);
        Long end = new Date().getTime();
        LOG.info(reply.getMessage());
        LOG.info("total time: {}", end-start2);
    }

}
