package com.testwa.distest.client.callback;import com.github.cosysoft.device.shell.AndroidSdk;import com.google.protobuf.ByteString;import com.testwa.distest.client.component.ADBCommandUtils;import com.testwa.distest.client.component.stfservice.KeyCode;import com.testwa.distest.client.DeviceClient;import lombok.extern.slf4j.Slf4j;import org.openqa.selenium.os.CommandLine;import java.io.IOException;/** * @Program: distest * @Description: * @Author: wen * @Create: 2018-05-08 16:36 **/@Slf4jpublic class ButtonEventCallback implements ICallBack<String> {    private DeviceClient client;    private int keyCode;    public ButtonEventCallback(DeviceClient client, int keyCode){        this.client = client;        this.keyCode = keyCode;    }    @Override    public void callback(ByteString bytes) {        String result = bytes.toStringUtf8();        log.debug(client+ ":callback-log" + result);        ADBCommandUtils.inputCode(client.getClientId(), keyCode, 5000L);    }}