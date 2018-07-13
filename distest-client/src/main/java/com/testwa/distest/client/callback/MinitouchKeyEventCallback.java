package com.testwa.distest.client.callback;import com.github.cosysoft.device.shell.AndroidSdk;import com.google.protobuf.ByteString;import com.testwa.distest.client.DeviceClient;import lombok.extern.slf4j.Slf4j;import org.openqa.selenium.os.CommandLine;import java.io.IOException;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-08 16:36 **/@Slf4jpublic class MinitouchKeyEventCallback implements ICallBack<String> {    private DeviceClient client;    public MinitouchKeyEventCallback(DeviceClient client){        this.client = client;    }    @Override    public void callback(ByteString bytes) {        String cmd = bytes.toStringUtf8();        log.debug(client+ ":callback-log" + cmd);        client.keyevent(Integer.parseInt(cmd));    }}