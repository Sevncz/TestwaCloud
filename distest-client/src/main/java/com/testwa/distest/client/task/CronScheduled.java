package com.testwa.distest.client.task;

import com.github.cosysoft.device.android.AndroidDevice;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.exception.DeviceNotReadyException;
import com.testwa.distest.client.service.GrpcClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by wen on 16/9/4.
 */
@Slf4j
@Component
public class CronScheduled {
    @Autowired
    private GrpcClientService grpcClientService;

    /**
     *@Description: android设备在线情况的补充检查
     *@Param: []
     *@Return: void
     *@Author: wen
     *@Date: 2018/5/8
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void androidInit() {
        Set<AndroidDevice> ads = AndroidHelper.getInstance().getAllDevices();
        ads.forEach(d -> {
            try {
                grpcClientService.initDevice(d.getSerialNumber());
            } catch (DeviceNotReadyException e) {
                log.error("", e);
            }
        });
    }

}
