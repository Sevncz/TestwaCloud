package com.testwa.distest.client.task;

import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.shell.ShellCommandException;
import com.google.protobuf.ByteString;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.client.control.client.MainClient;
import com.testwa.distest.client.model.TestwaDevice;
import com.testwa.distest.client.model.UserInfo;
import com.testwa.distest.client.service.HttpService;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.rpc.proto.Agent;
import com.testwa.distest.client.util.Constant;
import io.grpc.testwa.device.Device;
import io.grpc.testwa.device.DevicesRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wen on 16/9/4.
 */
@Component
public class TestwaScheduled {
    private static final Logger logger = LoggerFactory.getLogger(TestwaScheduled.class);

    public static BlockingQueue<String> screenUploadQueue = new ArrayBlockingQueue<>(10000);
    public static BlockingQueue<String> screenEmptyQueue = new ArrayBlockingQueue<>(10000);

    private static final Map<String, TestwaDevice> a_devices = new ConcurrentHashMap<>();

    @Value("${agent.web.url}")
    private String agentWebUrl;

    @Autowired
    private HttpService httpService;

    @Scheduled(cron = "0/10 * * * * ?")
    public void senderDevice() {
        if(MainClient.getWs().connected()){
            try{

                TreeSet<AndroidDevice> androidDevices = AndroidHelper.getInstance().getAllDevices();
                List<Device> result = new ArrayList<>();
                for(AndroidDevice ad : androidDevices){
                    logger.debug("send device", ad);
                    TestwaDevice device;
                    if(!a_devices.containsKey(ad.getSerialNumber())){
                        device = new TestwaDevice();
                        device.setSerial(ad.getSerialNumber());
                        device.setBrand(ad.runAdbCommand("shell getprop ro.product.brand"));
                        device.setCpuabi(ad.runAdbCommand("shell getprop ro.product.cpu.abi"));
                        device.setDensity(ad.getDevice().getDensity() + "");
                        if(ad.getTargetPlatform() != null){
                            device.setOsName(ad.getTargetPlatform().formatedName());
                        }
                        if(ad.getScreenSize() != null){
                            device.setWidth(String.valueOf(ad.getScreenSize().getWidth()));
                            device.setHeight(String.valueOf(ad.getScreenSize().getHeight()));
                        }
                        device.setCpuabi(ad.runAdbCommand("shell getprop ro.product.cpu.abi"));
                        device.setSdk(ad.runAdbCommand("shell getprop ro.build.version.sdk"));
                        device.setHost(ad.runAdbCommand("shell getprop ro.build.host"));
                        device.setModel(ad.runAdbCommand("shell getprop ro.product.model"));
                        device.setBrand(ad.runAdbCommand("shell getprop ro.product.brand"));
                        device.setVersion(ad.runAdbCommand("shell getprop ro.build.version.release"));
                        a_devices.put(ad.getSerialNumber(), device);
                    }else{
                        device = a_devices.get(ad.getDevice().getSerialNumber());
                    }
                    // 检查在线设备列表是否在缓存设备列表
                    if("ONLINE".equals(ad.getDevice().getState().name().toUpperCase())){
                        device.setStatus(Agent.Device.LineStatus.ON.name());
                    }else{
                        device.setStatus(Agent.Device.LineStatus.OFF.name());
                    }
                    result.add(device.toAgentDevice());
                }

                if(UserInfo.userId == null){
                    logger.error("userId was null");
                    return;
                }
                DevicesRequest devices = DevicesRequest.newBuilder()
                        .setCount(result.size())
                        .setUserId(UserInfo.userId)
                        .addAllDevice(result)
                        .build();
                MainClient.getWs().emit(WebsocketEvent.DEVICE, devices.toByteArray());
            }catch (ShellCommandException e){
                logger.error("Adb get props error", e);
            }
        }else{
            logger.error("Websocket was disconnect");
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    private void uploadScreenCaptrue() {
        for(;;){
            try {
                String filepath = screenUploadQueue.poll();
                if(StringUtils.isBlank(filepath)){
                    break;
                }
                Path p = Paths.get(filepath);

                String screenName = filepath.substring(Constant.localScreenshotPath.length() + 1);

                if(p.toFile().length() == 0){
                    try {
                        screenEmptyQueue.put(filepath);
                    } catch (InterruptedException e) {
                        logger.error("Put screenUploadQueue error", e);
                    }
                    continue;
                }

                byte[] img = Files.readAllBytes(p);
                ByteString bys = ByteString.copyFrom(img);
                Agent.ScreenCaptureFeedback message = Agent.ScreenCaptureFeedback
                        .newBuilder()
                        .setImg(bys)
                        .setName(screenName).build();
                // 这里异步上传文件
                httpService.postProto(String.format("%s/device/receive/screen", agentWebUrl), message.toByteArray());
                // feedback.runninglog.screen
//                这里会报错io.netty.handler.codec.CorruptedFrameException: Max frame length of 65536 has been exceeded.
//                TestwaSocket.getSocket().emit("feedback.runninglog.screen", message.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Scheduled(cron = "0/2 * * * * ?")
    private void processEmptyFile() {
        int len = screenEmptyQueue.size();
        for(int i=0;i<len;i++){
            String filepath = screenEmptyQueue.poll();
            if(StringUtils.isBlank(filepath)){
                break;
            }
            Path p = Paths.get(filepath);

            if(p.toFile().length() == 0){
                try {
                    screenEmptyQueue.put(filepath);
                } catch (InterruptedException e) {
                    logger.error("Put screenEmptyQueue error", e);
                }
            }else{
                try {
                    screenUploadQueue.put(filepath);
                } catch (InterruptedException e) {
                    logger.error("Put screenUploadQueue error", e);
                }
            }

        }
    }

}
