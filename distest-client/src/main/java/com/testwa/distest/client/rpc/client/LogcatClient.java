package com.testwa.distest.client.rpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.testwa.device.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by wen on 2016/12/4.
 */
public class LogcatClient {

    private static final Logger logger = LoggerFactory.getLogger(LogcatClient.class);

    private final ManagedChannel channel;
    private final LogcatGrpc.LogcatBlockingStub blockingStub;

    public LogcatClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext(true));
    }

    /** Construct client for accessing RouteGuide server using the existing channel. */
    LogcatClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = LogcatGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /** Say hello to server. */
    public void sender(LogcatRequest request) {
        CommonReply response;
        try {
            response = blockingStub.forward(request);
        } catch (StatusRuntimeException e) {
            logger.error("RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Logcat: " + response.getMessage());
    }

}
