package com.testwa.distest.client.event;import com.testwa.distest.client.grpc.GrpcClient;import com.testwa.distest.client.grpc.Gvice;import com.testwa.distest.client.model.UserInfo;import com.testwa.distest.client.service.GrpcClientService;import io.grpc.Channel;import io.rpc.testwa.task.CurrentExeInfoRequest;import lombok.extern.log4j.Log4j2;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.context.ApplicationListener;import org.springframework.scheduling.annotation.Async;import org.springframework.stereotype.Component;@Log4j2@Componentpublic class ExecutorCurrentInfoNotifyListener implements ApplicationListener<ExecutorCurrentInfoNotifyEvent> {    @GrpcClient("local-grpc-server")    private Channel serverChannel;    @Autowired    private GrpcClientService grpcClientService;    @Async    @Override    public void onApplicationEvent(ExecutorCurrentInfoNotifyEvent event) {        log.info("Event: device " + event.getDeviceId() + " connected");        grpcClientService.notifyServerCurrentTaskExecutorInfo(event.getDeviceId(), event.getTaskId(), event.getCurrScriptId(), event.getCurrTestCaseId());    }}