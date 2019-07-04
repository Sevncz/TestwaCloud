package com.testwa.distest.client.device.listener.callback.remote;

import com.google.protobuf.ByteString;
import com.testwa.distest.client.device.driver.IDeviceRemoteControlDriver;
import com.testwa.distest.client.device.listener.callback.IRemoteCommandCallBack;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeviceInfoCallback implements IRemoteCommandCallBack<ByteString> {
    private IDeviceRemoteControlDriver client;

    public DeviceInfoCallback(IDeviceRemoteControlDriver client){
        this.client = client;
    }

    @Override
    public void callback(ByteString bytes) {
        client.information();
    }
}

