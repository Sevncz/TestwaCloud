package com.testwa.distest.client.control.client.boost.impl;

import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.exception.DeviceNotFoundException;
import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.control.client.boost.MessageCallback;
import com.testwa.distest.client.control.client.boost.MessageException;
import com.testwa.distest.client.service.GrpcClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 16/8/20.
 */
@Component
public class StartRemoteClientCallbackImpl implements MessageCallback {

    @Autowired
    private Environment env;
    @Autowired
    private GrpcClientService grpcClientService;

    @Override
    public void done(Object o, MessageException e) throws MessageException {
        String serial = (String)o;
        try {
            AndroidDevice device = AndroidHelper.getInstance().getAndroidDevice(serial);
            if(device != null){
                grpcClientService.createRemoteClient(device.getDevice());
            }
        } catch (DeviceNotFoundException e1) {
            e1.printStackTrace();
        }
    }
}
