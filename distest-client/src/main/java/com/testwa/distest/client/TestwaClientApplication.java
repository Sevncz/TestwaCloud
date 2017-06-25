package com.testwa.distest.client;

import com.github.cosysoft.device.android.AndroidDevice;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.config.PortConfig;
import com.testwa.distest.client.control.client.BaseClient;
import com.testwa.distest.client.control.port.ApkPortProvider;
import com.testwa.distest.client.control.port.ScreenPortProvider;
import com.testwa.distest.client.control.port.TouchPortProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TreeSet;

/**
 * Created by wen on 16/8/14.
 */
@SpringBootApplication(exclude = MongoAutoConfiguration.class)
@EnableScheduling
public class TestwaClientApplication {

    public static void main(String[] args) {
        ScreenPortProvider.init(PortConfig.screenPortStart,
                PortConfig.screenPortEnd);
        TouchPortProvider.init(PortConfig.touchPortStart,
                PortConfig.touchPortEnd);
        ApkPortProvider.init(PortConfig.apkPortStart, PortConfig.apkPortEnd);

        SpringApplication.run(TestwaClientApplication.class, args);

        TreeSet<AndroidDevice> androidDevices = AndroidHelper.getInstance().getAllDevices();
        for(AndroidDevice ad : androidDevices) {
            BaseClient.startRemoteClient(ad.getDevice());
        }
    }

}
