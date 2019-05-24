package com.testwa.distest.client.event;import com.testwa.distest.client.DeviceClient;import com.testwa.distest.client.android.DeviceValidator;import com.testwa.distest.client.device.pool.DeviceManagerPool;import com.testwa.distest.client.exception.DeviceNotReadyException;import com.testwa.distest.client.service.DeviceGvice;import com.testwa.distest.client.service.GrpcClientService;import io.rpc.testwa.device.DeviceStatusChangeRequest;import io.rpc.testwa.device.DeviceType;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.context.ApplicationListener;import org.springframework.scheduling.annotation.Async;import org.springframework.stereotype.Component;@Slf4j@Componentpublic class DeviceStatusChangeListener implements ApplicationListener<DeviceStatusChangeEvent> {    @Autowired    private DeviceGvice deviceGvice;    @Autowired    private GrpcClientService grpcClientService;    @Autowired    private DeviceManagerPool deviceManagerPool;    @Async    @Override    public void onApplicationEvent(DeviceStatusChangeEvent event) {        if(StringUtils.isNotBlank(event.getDeviceId())){            deviceGvice.stateChange(event.getDeviceId(), event.getLineStatus());            if(DeviceStatusChangeRequest.LineStatus.ONLINE.equals(event.getLineStatus())) {//                try {//                    grpcClientService.initAndroidDevice(event.getDeviceId());//                } catch (DeviceNotReadyException e) {//                    e.printStackTrace();//                }                deviceManagerPool.getManager(event.getDeviceId(), DeviceType.ANDROID);            }else if(DeviceStatusChangeRequest.LineStatus.OFFLINE.equals(event.getLineStatus()) ||                    DeviceStatusChangeRequest.LineStatus.DISCONNECTED.equals(event.getLineStatus())) {                grpcClientService.destoryDeviceClient(event.getDeviceId());            }        }    }}