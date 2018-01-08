package com.testwa.distest.client.control.event;import com.github.cosysoft.device.android.AndroidDevice;import com.github.cosysoft.device.android.impl.AndroidDeviceStore;import com.github.cosysoft.device.exception.DeviceNotFoundException;import com.testwa.distest.client.grpc.GrpcClient;import com.testwa.distest.client.grpc.Gvice;import com.testwa.distest.client.service.GrpcClientService;import io.grpc.Channel;import lombok.extern.log4j.Log4j2;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.context.ApplicationListener;import org.springframework.scheduling.annotation.Async;import org.springframework.stereotype.Component;@Log4j2@Componentpublic class DeviceConnectedListener implements ApplicationListener<DeviceConnectedEvent> {    @GrpcClient("local-grpc-server")    private Channel serverChannel;    @Autowired    private GrpcClientService grpcClientService;    @Async    @Override    public void onApplicationEvent(DeviceConnectedEvent event) {        log.info("Event: device " + event.getDeviceId() + " connected");        int maxTime = 10;        try {            AndroidDevice device = AndroidDeviceStore.getInstance().getDeviceBySerial(event.getDeviceId());            if(device != null){                while(true){                    if(maxTime == 0) break;                    if(device.isDeviceReady()){                        grpcClientService.createRemoteClient(device.getDevice());                        break;                    }                    try {                        maxTime--;                        Thread.sleep(100);                    } catch (InterruptedException e) {                        e.printStackTrace();                    }                }            }        }catch (DeviceNotFoundException e){            log.error("Event: device " + event.getDeviceId() + " not found");        }    }}