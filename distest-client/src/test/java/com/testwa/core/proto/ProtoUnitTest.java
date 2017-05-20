package com.testwa.distest.client.proto;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.testwa.helloworld.GreeterGrpc;
import io.grpc.testwa.helloworld.HelloReply;
import io.grpc.testwa.helloworld.HelloRequest;
import org.junit.Test;

import java.util.Date;

/**
 * Created by wen on 16/8/13.
 */
public class ProtoUnitTest {
    private ManagedChannel mChannel;

    private String mHost = "localhost";
    private String mMessage = "hello";
    private int mPort = 6565;

    @Test
    public void testClient() {

        try {
            mChannel = ManagedChannelBuilder.forAddress(mHost, mPort)
                    .usePlaintext(true)
                    .build();
            Long start1 = new Date().getTime();
            GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(mChannel);
            Long start2 = new Date().getTime();
            HelloRequest message = HelloRequest.newBuilder().setName(mMessage).build();
            HelloReply reply = stub.sayHello(message);
            Long end = new Date().getTime();
            System.out.println(reply.getMessage());
            System.out.println(end-start1);
            System.out.println(end-start2);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
