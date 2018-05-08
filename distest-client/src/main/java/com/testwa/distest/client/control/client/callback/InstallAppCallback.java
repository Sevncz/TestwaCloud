package com.testwa.distest.client.control.client.callback;import com.google.protobuf.ByteString;import com.testwa.distest.client.control.client.DeviceClient;import lombok.extern.slf4j.Slf4j;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-08 16:36 **/@Slf4jpublic class InstallAppCallback implements ICallBack<String> {    private DeviceClient client;    public InstallAppCallback(DeviceClient client){        this.client = client;    }    @Override    public void callback(ByteString bytes) {        String command = bytes.toStringUtf8();        log.info(client + ":callback-log " + command);        client.installApp(command);    }}