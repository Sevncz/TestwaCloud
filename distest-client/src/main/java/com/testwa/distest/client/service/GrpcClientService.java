package com.testwa.distest.client.service;import com.android.ddmlib.IDevice;import com.github.cosysoft.device.android.AndroidDevice;import com.github.cosysoft.device.shell.ShellCommandException;import com.testwa.distest.client.ApplicationContextUtil;import com.testwa.distest.client.control.client.Clients;import com.testwa.distest.client.control.client.RemoteClient;import com.testwa.distest.client.grpc.GrpcClient;import com.testwa.distest.client.grpc.Gvice;import com.testwa.distest.client.model.TestwaDevice;import com.testwa.distest.client.model.UserInfo;import io.grpc.Channel;import io.rpc.testwa.device.ConnectedRequest;import lombok.extern.log4j.Log4j2;import org.apache.commons.lang3.StringUtils;import org.springframework.beans.factory.annotation.Value;import org.springframework.core.env.Environment;import org.springframework.scheduling.annotation.Async;import org.springframework.stereotype.Service;import java.io.IOException;import java.net.URISyntaxException;@Log4j2@Servicepublic class GrpcClientService {    @GrpcClient("local-grpc-server")    private Channel serverChannel;    @Value("${distest.agent.resources}")    private String resourcesPath;    @Async    public void createRemoteClient(IDevice device){        Environment env = ApplicationContextUtil.getApplicationContext().getEnvironment();        String url = env.getProperty("agent.socket.url");        String wsUrl = String.format("%s?token=%s&type=device&serial=%s&from=BaseClient", url, UserInfo.token, device.getSerialNumber());        try {            RemoteClient remoteClient = new RemoteClient(wsUrl, "", device.getSerialNumber(), serverChannel, resourcesPath);            Clients.add(device.getSerialNumber(), remoteClient);        } catch (IOException | URISyntaxException e) {            e.printStackTrace();        }    }    @Async    public void initDevice(AndroidDevice dev){        log.info("device【" + dev.getSerialNumber() + "】init sending......");        while (true) {            // 设备没有同意连接，不做设备信息更新，等待下次检查时更新            try {                String brand = dev.runAdbCommand("shell getprop ro.product.brand");                if(StringUtils.isEmpty(brand) || StringUtils.isEmpty(UserInfo.token)){                    try {                        Thread.sleep(100);                    } catch (InterruptedException e) {                        e.printStackTrace();                    }                    continue;                }                String cpuabi = dev.runAdbCommand("shell getprop ro.product.cpu.abi");                String sdk = dev.runAdbCommand("shell getprop ro.build.version.sdk");                String host = dev.runAdbCommand("shell getprop ro.build.host");                String model = dev.runAdbCommand("shell getprop ro.product.dto");                String version = dev.runAdbCommand("shell getprop ro.build.version.release");                String density = dev.getDevice().getDensity() + "";                String targetPlatform = "";                if (dev.getTargetPlatform() != null) {                    targetPlatform = dev.getTargetPlatform().formatedName();                }                String width = "";                String height = "";                if (dev.getScreenSize() != null) {                    width = String.valueOf(dev.getScreenSize().getWidth());                    height = String.valueOf(dev.getScreenSize().getHeight());                }                ConnectedRequest reuqest = ConnectedRequest.newBuilder()                        .setDeviceId(dev.getSerialNumber())                        .setBrand(brand)                        .setCpuabi(cpuabi)                        .setDensity(density)                        .setHeight(height)                        .setWidth(width)                        .setHost(host)                        .setModel(model)                        .setOsName(targetPlatform)                        .setSdk(sdk)                        .setToken(UserInfo.token)                        .setVersion(version)                        .build();                Gvice.deviceService(serverChannel).connect(reuqest);                break;            }catch (ShellCommandException e){                try {                    Thread.sleep(100);                } catch (InterruptedException e1) {                    e.printStackTrace();                }                continue;            }        }    }}