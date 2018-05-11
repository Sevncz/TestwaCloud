package com.testwa.distest.client.task;

import com.testwa.distest.client.service.GrpcClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
