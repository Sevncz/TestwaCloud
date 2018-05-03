package com.testwa.distest.client.task;

import com.testwa.distest.client.grpc.GrpcClient;
import com.testwa.distest.client.model.TestwaDevice;
import com.testwa.distest.client.service.HttpService;
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
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wen on 16/9/4.
 */
@Slf4j
@Component
public class TestwaScheduled {
    public static BlockingQueue<String> screenUploadQueue = new ArrayBlockingQueue<>(10000);
    public static BlockingQueue<String> screenEmptyQueue = new ArrayBlockingQueue<>(10000);

    public static Map<String, TestwaDevice> a_devices = new ConcurrentHashMap<>();

    @Value("${cloud.web.url}")
    private String agentWebUrl;

    @Autowired
    private HttpService httpService;
    @Autowired
    private Environment env;
    @GrpcClient("local-grpc-server")
    private Channel serverChannel;


    @Scheduled(cron = "0/10 * * * * ?")
    public void senderDevice() {
//        if (MainSocket.getSocket() != null && MainSocket.getSocket().connected()) {
//            try {
//                TreeSet<AndroidDevice> androidDevices = AndroidHelper.getInstance().getAllDevices();
//                // 上报列表，当有新的设备信息获取到时
//                List<Device> devicesToReport = new ArrayList<>();
//                for (AndroidDevice ad : androidDevices) {
//                    logger.debug("send device", ad);
//                    TestwaDevice device;
//                    if (!a_devices.containsKey(ad.getSerialNumber())) {
//                        device = new TestwaDevice();
//                        device.setSerial(ad.getSerialNumber());
//                        device.setBrand(ad.runAdbCommand("shell getprop ro.product.brand"));
//                        if (device.getBrand().isEmpty()) {
//                            // 设备没有同意连接，不做设备信息更新，等待下次检查时更新
//                            continue;
//                        }
//                        device.setCpuabi(ad.runAdbCommand("shell getprop ro.product.cpu.abi"));
//                        device.setDensity(ad.getDevice().getDensity() + "");
//                        if (ad.getTargetPlatform() != null) {
//                            device.setOsName(ad.getTargetPlatform().formatedName());
//                        }
//                        if (ad.getScreenSize() != null) {
//                            device.setWidth(String.valueOf(ad.getScreenSize().getWidth()));
//                            device.setHeight(String.valueOf(ad.getScreenSize().getHeight()));
//                        }
//                        device.setCpuabi(ad.runAdbCommand("shell getprop ro.product.cpu.abi"));
//                        device.setSdk(ad.runAdbCommand("shell getprop ro.build.version.sdk"));
//                        device.setHost(ad.runAdbCommand("shell getprop ro.build.host"));
//                        device.setModel(ad.runAdbCommand("shell getprop ro.product.dto"));
//                        device.setBrand(ad.runAdbCommand("shell getprop ro.product.brand"));
//                        device.setVersion(ad.runAdbCommand("shell getprop ro.build.version.release"));
//
//                        device.setStatus("ON");
//                        a_devices.put(ad.getSerialNumber(), device);
//                        devicesToReport.add(device.toAgentDevice());
//                    }
//                    // 检查在线设备列表是否在缓存设备列表 无缓存信息
////                    if("ONLINE".equals(ad.getDevice().getState().name().toUpperCase())){
////                        device.setStatus(Agent.Device.LineStatus.ON.name());
////                    }else{
////                        device.setStatus(Agent.Device.LineStatus.OFF.name());
////                    }
//                }
//
//                if (UserInfo.token == null) {
//                    logger.error("token was null");
//                    return;
//                }
//                if (devicesToReport.size() > 0) {
//                    // 有新的设备需要上报
//                    DevicesRequest request = DevicesRequest.newBuilder()
//                            .setCount(devicesToReport.size())
//                            .setUserId(UserInfo.token)
//                            .addAllDevice(devicesToReport)
//                            .build();
//
//                    Gvice.deviceService(serverChannel).all(request);
//                }
//            } catch (ShellCommandException e) {
//                logger.error("Adb get props error", e);
//            }
//        } else {
//            logger.error("Websocket was disconnect");
//            // 与服务断开连接后，清空缓存的上报列表，待重连后， 重新上报。
//            a_devices = new ConcurrentHashMap<>();
//        }
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
