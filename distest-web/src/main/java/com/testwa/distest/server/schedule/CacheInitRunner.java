package com.testwa.distest.server.schedule;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 启动的时候对redis的部分key进行初始化动作
 * Created by wen on 16/9/7.
 */
@Log4j2
@Component
public class CacheInitRunner implements CommandLineRunner {

//    @Autowired
//    private RemoteClientService remoteClientService;


    @Override
    public void run(String... strings) throws Exception {
//        remoteClientService.delDeviceForClient();
//        remoteClientService.delShareDevice();
//        remoteClientService.delMainInfo();
//        remoteClientService.delOnstartScreenDevice();
        log.info("clean cache key");
    }
}
