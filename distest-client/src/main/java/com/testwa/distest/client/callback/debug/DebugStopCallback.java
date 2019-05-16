package com.testwa.distest.client.callback.debug;import com.google.protobuf.ByteString;import com.testwa.distest.client.DeviceClient;import com.testwa.distest.client.callback.ICallBack;import lombok.extern.slf4j.Slf4j;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-08 16:36 **/@Slf4jpublic class DebugStopCallback implements ICallBack<String> {    private DeviceClient client;    public DebugStopCallback(DeviceClient client){        this.client = client;    }    @Override    public void callback(ByteString bytes) {        log.debug(client+ ":callback-log");        client.debugStop();    }}