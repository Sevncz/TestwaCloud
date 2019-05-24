package com.testwa.distest.client.callback.task;import com.google.protobuf.ByteString;import com.testwa.distest.client.DeviceClient;import com.testwa.distest.client.callback.ICallBack;import lombok.extern.slf4j.Slf4j;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-08 16:36 **/@Slf4jpublic class TaskStartCallback implements ICallBack<String> {    private DeviceClient client;    public TaskStartCallback(DeviceClient client){        this.client = client;    }    @Override    public void callback(ByteString bytes) {        String cmd = bytes.toStringUtf8();        log.debug("{} :callback-log 启动任务 {}", client.getClientId(), cmd);        client.startFunctionalTask(cmd);    }}