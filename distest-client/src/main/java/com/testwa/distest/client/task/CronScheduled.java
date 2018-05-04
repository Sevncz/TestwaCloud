package com.testwa.distest.client.task;

import com.testwa.distest.client.grpc.GrpcClient;
import com.testwa.distest.client.component.Constant;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by wen on 16/9/4.
 */
@Slf4j
@Component
public class CronScheduled {
    public static BlockingQueue<String> screenUploadQueue = new ArrayBlockingQueue<>(10000);
    public static BlockingQueue<String> screenEmptyQueue = new ArrayBlockingQueue<>(10000);

    @Autowired
    private Environment env;
    @GrpcClient("local-grpc-server")
    private Channel serverChannel;
    @Value("${distest.agent.resources}")
    private String resourcesPath;


    /**
     *@Description:  获得ios设备信息
     *@Param: []
     *@Return: void
     *@Author: wen
     *@Date: 2018/5/4
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void iphoneSender() {


    }

    @Scheduled(cron = "0/5 * * * * ?")
    private void uploadScreenCaptrue() {
        for (; ; ) {
            try {
                String filepath = screenUploadQueue.poll();
                if (StringUtils.isBlank(filepath)) {
                    break;
                }
                Path p = Paths.get(filepath);

                String screenName = filepath.substring(Constant.localScreenshotPath.length() + 1);

                if (p.toFile().length() == 0) {
                    try {
                        screenEmptyQueue.put(filepath);
                    } catch (InterruptedException e) {
                        log.error("Put screenUploadQueue error", e);
                    }
                    continue;
                }

//                byte[] img = Files.readAllBytes(p);
//                ByteString bys = ByteString.copyFrom(img);
//                Agent.ScreenCaptureFeedback message = Agent.ScreenCaptureFeedback
//                        .newBuilder()
//                        .setImg(bys)
//                        .setName(screenName).build();
                // 这里异步上传文件
//                httpService.postProto(String.format("%s/device/receive/screen", agentWebUrl), message.toByteArray());
                // feedback.runninglog.screen
//                这里会报错io.netty.handler.codec.CorruptedFrameException: Max frame length of 65536 has been exceeded.
//                TestwaSocket.getSocket().emit("feedback.runninglog.screen", message.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Scheduled(cron = "0/2 * * * * ?")
    private void processEmptyFile() {
        int len = screenEmptyQueue.size();
        for (int i = 0; i < len; i++) {
            String filepath = screenEmptyQueue.poll();
            if (StringUtils.isBlank(filepath)) {
                break;
            }
            Path p = Paths.get(filepath);

            if (p.toFile().length() == 0) {
                try {
                    screenEmptyQueue.put(filepath);
                } catch (InterruptedException e) {
                    log.error("Put screenEmptyQueue error", e);
                }
            } else {
                try {
                    screenUploadQueue.put(filepath);
                } catch (InterruptedException e) {
                    log.error("Put screenUploadQueue error", e);
                }
            }

        }
    }

}
