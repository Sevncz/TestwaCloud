package com.testwa.distest.client.callback;import com.google.protobuf.ByteString;import com.testwa.distest.client.DeviceClient;import lombok.extern.slf4j.Slf4j;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-08 16:36 **/@Slf4jpublic class ScreenWaitCallback implements ICallBack<String> {    private DeviceClient client;    public ScreenWaitCallback(DeviceClient client){        this.client = client;    }    @Override    public void callback(ByteString bytes) {        String result = bytes.toStringUtf8();        log.debug(client+ ":callback-log" + result);        client.setWaitting(false);    }}