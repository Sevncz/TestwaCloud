package com.testwa.distest.client.callback;import com.google.protobuf.ByteString;import com.testwa.distest.client.DeviceClient;import com.testwa.distest.client.android.ADBCommandUtils;import lombok.extern.slf4j.Slf4j;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-08 16:36 **/@Slf4jpublic class OpenWebCallback implements ICallBack<String> {    private DeviceClient client;    public OpenWebCallback(DeviceClient client){        this.client = client;    }    @Override    public void callback(ByteString bytes) {        String cmd = bytes.toStringUtf8();        log.debug(client+ ":callback-log  " + cmd);        ADBCommandUtils.openWeb(client.getClientId(), cmd);    }}