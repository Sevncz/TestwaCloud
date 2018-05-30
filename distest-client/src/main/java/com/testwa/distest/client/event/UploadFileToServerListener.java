package com.testwa.distest.client.event;import com.testwa.distest.client.service.GrpcClientService;import lombok.extern.slf4j.Slf4j;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.context.ApplicationListener;import org.springframework.scheduling.annotation.Async;import org.springframework.stereotype.Component;@Slf4j@Componentpublic class UploadFileToServerListener implements ApplicationListener<UploadFileToServerEvent> {    @Autowired    private GrpcClientService grpcClientService;    @Async    @Override    public void onApplicationEvent(UploadFileToServerEvent event) {        log.info("Event: Upload file to server");        UploadFileToServerEvent.FileType type = event.getType();        String deviceId = event.getDeviceId();        switch (type) {            case LOGCAT://                log.info("Upload logcat to server, path: {}", event.getFilePath());//                grpcClientService.logcatUpload(event.getTaskCode(), deviceId, event.getFilePath());                break;            case APPIUMLOG:                log.info("Upload appium to server, path: {}", event.getFilePath());                grpcClientService.appiumLogUpload(event.getTaskId(), deviceId, event.getFilePath());                break;            case SCREENSHOOT:                break;        }    }}