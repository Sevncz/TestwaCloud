package com.testwa.distest.client.rpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.testwa.device.CommonReply;
import io.grpc.testwa.device.ScreenCaptureGrpc;
import io.grpc.testwa.device.ScreenCaptureRequest;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by wen on 2016/12/4.
 */
public class ScreenCaptureClient {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ScreenCaptureClient.class);

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
            logger.error("RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("S: " + response.getMessage());
    }

}
