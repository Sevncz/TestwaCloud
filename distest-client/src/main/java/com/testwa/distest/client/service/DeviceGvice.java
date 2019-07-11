package com.testwa.distest.client.service;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.Channel;
import io.rpc.testwa.device.CommonReply;
import io.rpc.testwa.device.DeviceStatusChangeRequest;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.client.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

/**
 * 负责设备状态更改通信
 * @author wen
 * @create 2019-05-15 20:05
 */
@Slf4j
@Component
public class DeviceGvice {

    @GrpcClient("grpc-server")
    private Channel serverChannel;

    public void stateChange(String deviceId, DeviceStatusChangeRequest.LineStatus status) {
        DeviceStatusChangeRequest request = DeviceStatusChangeRequest.newBuilder()
                .setDeviceId(deviceId)
                .setStatus(status)
                .build();
        ListenableFuture<CommonReply> reply = Gvice.deviceService(serverChannel).stateChange(request);
        try {
            CommonReply result = reply.get();
            result.getMessage();
        } catch (InterruptedException | ExecutionException e) {
            log.info("{} 同步状态失败", deviceId, e);
        }
    }


}
