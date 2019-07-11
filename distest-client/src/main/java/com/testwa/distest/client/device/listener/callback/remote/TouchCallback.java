package com.testwa.distest.client.device.listener.callback.remote;import com.google.protobuf.ByteString;import com.testwa.distest.client.device.driver.IDeviceRemoteControlDriver;import com.testwa.distest.client.device.listener.callback.IRemoteCommandCallBack;import lombok.extern.slf4j.Slf4j;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-08 16:36 **/@Slf4jpublic class TouchCallback implements IRemoteCommandCallBack<ByteString> {    private IDeviceRemoteControlDriver client;    public TouchCallback(IDeviceRemoteControlDriver client){        this.client = client;    }    @Override    public void callback(ByteString bytes) {        String cmd = bytes.toStringUtf8();        log.debug(client+ ":callback-log" + cmd);        client.touch(cmd);    }}