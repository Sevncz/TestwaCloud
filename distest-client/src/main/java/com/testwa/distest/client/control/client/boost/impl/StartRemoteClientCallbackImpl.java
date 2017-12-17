package com.testwa.distest.client.control.client.boost.impl;

import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.exception.DeviceNotFoundException;
import com.google.protobuf.InvalidProtocolBufferException;
import com.testwa.distest.client.android.AndroidHelper;
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
    private String token;

    @Autowired
    private Environment env;

    @Override
    public void done(Object o, MessageException e) throws MessageException {
        String serial = (String)o;
        try {
            String url = env.getProperty("agent.socket.url");
            String webHost = env.getProperty("grpc.host");
            Integer webPort = Integer.parseInt(env.getProperty("grpc.port"));
            // **
            // 这里的token来不及获取，所以token是null
            // **
            String wsUrl = String.format("%s?token=%s&type=device&serial=%s", url, this.token, serial);

            AndroidDevice device = AndroidHelper.getInstance().getAndroidDevice(serial);
            if(device != null){
                RemoteClient remoteClient = new RemoteClient(wsUrl, "", serial, webHost, webPort);
                Clients.add(serial, remoteClient);
            }
        } catch (InvalidProtocolBufferException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }catch (DeviceNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

}
