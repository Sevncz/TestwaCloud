package com.testwa.distest.client.control.client.callback;import com.google.protobuf.ByteString;import com.testwa.distest.client.control.client.DeviceClient;import lombok.extern.slf4j.Slf4j;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-08 16:36 **/@Slf4jpublic class ComponentStopCallback implements ICallBack<String> {    private DeviceClient client;    public ComponentStopCallback(DeviceClient client){        this.client = client;    }    @Override    public void callback(ByteString bytes) {        String result = bytes.toStringUtf8();        log.debug(client+ ":callback-log" + result);        client.stopMinicap();        client.stopMinitouch();    }}