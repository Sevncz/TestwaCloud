package com.testwa.distest.client.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.util.concurrent.ListenableFuture;
import com.testwa.core.script.Function;
import com.testwa.core.script.ScriptGenerator;
import com.testwa.core.script.snippet.ScriptActionEnum;
import com.testwa.core.script.snippet.ScriptCode;
import com.testwa.core.script.util.VoUtil;
import com.testwa.core.script.vo.ScriptActionVO;
import com.testwa.core.script.vo.ScriptCaseVO;
import com.testwa.core.script.vo.ScriptFunctionVO;
import com.testwa.core.script.vo.TaskVO;
import com.testwa.core.shell.UTF8CommonExecs;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.component.appium.manager.CustomAppiumManager;
import com.testwa.distest.client.component.appium.pool.CustomAppiumManagerPool;
import com.testwa.distest.client.util.PortUtil;
import io.grpc.Channel;
import io.rpc.testwa.device.CommonReply;
import io.rpc.testwa.device.DeviceStatusChangeRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.devh.springboot.autoconfigure.grpc.client.GrpcClient;
import org.apache.commons.exec.CommandLine;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ScriptGenerator scriptGenerator;
    @Autowired
    private ScriptCode scriptCodePython;
    @Autowired
    private CustomAppiumManagerPool customAppiumManagerPool;

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
