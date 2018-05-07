package com.testwa.distest.client.event;import com.github.cosysoft.device.android.AndroidDevice;import com.github.cosysoft.device.android.impl.AndroidDeviceStore;import com.github.cosysoft.device.exception.DeviceNotFoundException;import com.testwa.distest.client.control.client.Clients;import com.testwa.distest.client.service.GrpcClientService;import lombok.extern.slf4j.Slf4j;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.context.ApplicationListener;import org.springframework.scheduling.annotation.Async;import org.springframework.stereotype.Component;@Slf4j@Componentpublic class DeviceOnlineListener implements ApplicationListener<DeviceConnectedEvent> {    @Autowired    private GrpcClientService grpcClientService;    @Async    @Override    public void onApplicationEvent(DeviceConnectedEvent event) {        log.info("Event: 设备 " + event.getDeviceId() + " 上线");        grpcClientService.deviceOnline(event.getDeviceId());    }}