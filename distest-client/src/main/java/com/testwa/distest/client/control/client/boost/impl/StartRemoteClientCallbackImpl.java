package com.testwa.distest.client.control.client.boost.impl;

import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.exception.DeviceNotFoundException;
import com.google.protobuf.InvalidProtocolBufferException;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.control.client.BaseClient;
import com.testwa.distest.client.control.client.Clients;
import com.testwa.distest.client.control.client.RemoteClient;
import com.testwa.distest.client.control.client.boost.MessageCallback;
import com.testwa.distest.client.control.client.boost.MessageException;
import com.testwa.distest.client.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by wen on 16/8/20.
 */
@Component
public class StartRemoteClientCallbackImpl implements MessageCallback {

    @Autowired
    private Environment env;

    @Override
    public void done(Object o, MessageException e) throws MessageException {
        String serial = (String)o;
        try {
            AndroidDevice device = AndroidHelper.getInstance().getAndroidDevice(serial);
            if(device != null){
                BaseClient.startRemoteClient(device.getDevice());
            }
        } catch (DeviceNotFoundException e1) {
            e1.printStackTrace();
        }
    }
}
