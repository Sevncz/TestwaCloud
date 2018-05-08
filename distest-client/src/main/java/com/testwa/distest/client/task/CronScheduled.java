package com.testwa.distest.client.task;

import com.github.cosysoft.device.android.AndroidDevice;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.grpc.GrpcClient;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.service.GrpcClientService;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by wen on 16/9/4.
 */
@Slf4j
@Component
public class CronScheduled {
    @Autowired
    private GrpcClientService grpcClientService;

    /**
     *@Description: android设备在线情况的补充
     *@Param: []
     *@Return: void
     *@Author: wen
     *@Date: 2018/5/8
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void androidInit() {
//        Set<AndroidDevice> devices = AndroidHelper.getInstance().getAllDevices();
//        devices.forEach(d -> grpcClientService.deviceOnline(d.getSerialNumber()));
    }

}
