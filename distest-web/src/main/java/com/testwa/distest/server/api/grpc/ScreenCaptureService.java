package com.testwa.distest.server.api.grpc;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.testwa.distest.server.LogInterceptor;
import com.testwa.distest.server.api.WSFlagEnum;
import com.testwa.distest.server.grpc.GRpcService;
import com.testwa.distest.server.service.redis.TestwaDeviceRedisService;
import io.grpc.stub.StreamObserver;
import io.grpc.testwa.device.CommonReply;
import io.grpc.testwa.device.ScreenCaptureGrpc;
import io.grpc.testwa.device.ScreenCaptureRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

/**
 * Created by wen on 2016/12/4.
 */
@GRpcService(interceptors = { LogInterceptor.class })
public class ScreenCaptureService extends ScreenCaptureGrpc.ScreenCaptureImplBase{
    private static final Logger log = LoggerFactory.getLogger(ScreenCaptureService.class);

    @Autowired
    private TestwaDeviceRedisService deviceRedisService;

    @Autowired
    private StringRedisTemplate template;

    @Autowired
    private SocketIOServer server;

    @Override
    public void forward(ScreenCaptureRequest request, StreamObserver<CommonReply> responseObserver) {
//        deviceRedisService.saveImgData(request);
        String serial = request.getSerial();
        String sessionId = template.opsForValue().get(String.format("flag.%s.%s", WSFlagEnum.SCREEN.getValue(), serial));

        SocketIOClient client = server.getClient(UUID.fromString(sessionId));
        byte[] data = request.getImg().toByteArray();
        log.info(" GET data length {}", data.length);
        client.sendEvent("minicap", data);

        final CommonReply.Builder replyBuilder = CommonReply.newBuilder().setMessage("OK ");
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }
}
