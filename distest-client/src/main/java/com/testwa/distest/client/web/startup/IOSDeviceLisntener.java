package com.testwa.distest.client.web.startup;

import com.testwa.core.shell.UTF8CommonExecs;
import com.testwa.distest.client.component.Constant;
import com.testwa.distest.client.grpc.GrpcClient;
import com.testwa.distest.client.model.IOSDevice;
import com.testwa.distest.client.service.GrpcClientService;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by wen on 16/9/4.
 */
@Slf4j
@Component
public class IOSDeviceLisntener implements CommandLineRunner {

    @Autowired
    private GrpcClientService grpcClientService;
    @Value("${distest.agent.resources}")
    private String resourcesPath;

    @Override
    public void run(String... strings) {
        Thread IOSDeviceInitialThread = new Thread(new IOSDeviceInitial());
        IOSDeviceInitialThread.start();
    }

    class IOSDeviceInitial implements Runnable {

        public void run() {
            while(true) {
                try {
                    Path iosDeployPath = Paths.get(resourcesPath, Constant.getIOSDeploy());
                    CommandLine cli = new CommandLine(iosDeployPath.toFile());
                    cli.addArgument("-c");
                    UTF8CommonExecs executable = new UTF8CommonExecs(cli);
                    executable.exec();
                    String output = executable.getOutput();
//                    log.info(output);
                    parseIOSDeviceInfo(output);
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    /**
     *@Description: 解析设备信息
     *@Param: [output]
     *@Return: void
     *@Author: wen
     *@Date: 2018/5/4
     */
    private void parseIOSDeviceInfo(String output) {
        /**
         * [main] INFO com.testwa.distest.client.task.CronScheduled - [....] Waiting up to 5 seconds for iOS device to be connected
         * [....] Found 62dbafcdf99e337d14515b67d869b3ec4941a00a (N61AP, iPhone 6 (GSM), iphoneos, arm64) a.k.a. 'iSevncz' connected through USB.
         */
        String[] lines = output.split("\n");
        for(String deviceLine : lines) {
            if(deviceLine.startsWith("[....] Found")){
                String uuid = deviceLine.split(" ")[2];
                String extraInfoLine = deviceLine.substring(deviceLine.indexOf("(") + 1, deviceLine.lastIndexOf(")"));
                log.info("uuid: {}, extraInfo: {}", uuid, extraInfoLine);
                String[] extraInfoList = extraInfoLine.split(",");
                IOSDevice d = new IOSDevice();
                d.setDeviceId(uuid);
                d.setModel(extraInfoList[1]);
                d.setCpuabi(extraInfoList[3]);
                grpcClientService.initDevice(d);
            }
        }
    }


    public static void main(String[] args) {
        try {
            String shell = "/Users/wen/IdeaProjects/distest/distest-client/bin/resources/ios-deploy/ios-deploy";
            CommandLine cli = new CommandLine(new File(shell));
            cli.addArgument("-c");

            UTF8CommonExecs executable = new UTF8CommonExecs(cli);
            executable.exec();
            String output = executable.getOutput();
            log.info(output);
//            parseIOSDeviceInfo(output);
        } catch (Exception e) {
        }
    }
}
