package com.testwa.distest.server.api.grpc;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.distest.server.LogInterceptor;
import com.testwa.distest.server.api.WSFlagEnum;
import com.testwa.distest.server.grpc.GRpcService;
import com.testwa.distest.server.service.redis.TestwaDeviceRedisService;
import io.grpc.stub.StreamObserver;
import io.grpc.testwa.device.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

/**
 * Created by wen on 2016/12/4.
 */
@GRpcService(interceptors = { LogInterceptor.class })
public class LogcatService extends LogcatGrpc.LogcatImplBase{
    private static final Logger log = LoggerFactory.getLogger(LogcatService.class);

    @Autowired
    private TestwaDeviceRedisService deviceRedisService;

    @Autowired
    private StringRedisTemplate template;

    @Autowired
    private SocketIOServer server;

    @Override
    public void forward(LogcatRequest request, StreamObserver<CommonReply> responseObserver) {
//        deviceRedisService.saveImgData(request);
        String serial = request.getSerial();
        String sessionId = template.opsForValue().get(String.format("flag.%s.%s", WSFlagEnum.LOGCAT.getValue(), serial));
        byte[] data = request.getContent().toByteArray();
        if(StringUtils.isNotBlank(sessionId)){
            SocketIOClient client = server.getClient(UUID.fromString(sessionId));
            if(client != null){
                client.sendEvent("logcat", new String(data));
            }
        }
        log.info(" GET data length {}", data.length);

        final CommonReply.Builder replyBuilder = CommonReply.newBuilder().setMessage("OK ");
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }


}
