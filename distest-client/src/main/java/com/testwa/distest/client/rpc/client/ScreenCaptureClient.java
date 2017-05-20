package com.testwa.distest.client.rpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.testwa.device.CommonReply;
import io.grpc.testwa.device.ScreenCaptureGrpc;
import io.grpc.testwa.device.ScreenCaptureRequest;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by wen on 2016/12/4.
 */
public class ScreenCaptureClient {

    private static final Logger logger = Logger.getLogger(ScreenCaptureClient.class.getName());

    private final ManagedChannel channel;
    private final ScreenCaptureGrpc.ScreenCaptureBlockingStub blockingStub;

    public ScreenCaptureClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext(true));
    }

    /** Construct client for accessing RouteGuide server using the existing channel. */
    ScreenCaptureClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = ScreenCaptureGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /** Say hello to server. */
    public void sender(ScreenCaptureRequest request) {
        CommonReply response;
        try {
            response = blockingStub.sender(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("S: " + response.getMessage());
    }

}
