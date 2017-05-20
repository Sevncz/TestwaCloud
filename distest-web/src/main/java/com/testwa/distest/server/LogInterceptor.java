package com.testwa.distest.server;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by jamessmith on 9/7/16.
 */
@Component
public class LogInterceptor implements ServerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LogInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        log.info(call.getMethodDescriptor().getFullMethodName());
        return next.startCall(call, headers);
    }
}