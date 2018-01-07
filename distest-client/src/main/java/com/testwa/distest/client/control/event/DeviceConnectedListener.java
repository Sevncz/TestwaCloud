package com.testwa.distest.client.control.event;import com.github.cosysoft.device.android.AndroidDevice;import com.testwa.distest.client.android.AndroidHelper;import com.testwa.distest.client.grpc.GrpcClient;import com.testwa.distest.client.service.GrpcClientService;import io.grpc.Channel;import lombok.extern.log4j.Log4j2;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.context.ApplicationListener;import org.springframework.scheduling.annotation.Async;import org.springframework.stereotype.Component;@Log4j2@Componentpublic class DeviceConnectedListener implements ApplicationListener<DeviceConnectedEvent> {    @GrpcClient("local-grpc-server")    private Channel serverChannel;    @Autowired    private GrpcClientService grpcClientService;    @Async    @Override    public void onApplicationEvent(DeviceConnectedEvent event) {        log.info("device 【" + event.getDeviceId() + "】connected");        AndroidDevice dev = AndroidHelper.getInstance().getAndroidDevice(event.getDeviceId());        if(dev != null){            grpcClientService.initDevice(dev);        }    }}