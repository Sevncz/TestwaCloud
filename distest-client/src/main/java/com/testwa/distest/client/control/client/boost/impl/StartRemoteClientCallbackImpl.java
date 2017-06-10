package com.testwa.distest.client.control.client.boost.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import com.testwa.distest.client.boost.TestwaException;
import com.testwa.distest.client.boost.TestwaNotificationCallback;
import com.testwa.distest.client.control.client.Clients;
import com.testwa.distest.client.control.client.MainClient;
import com.testwa.distest.client.control.client.RemoteClient;
import com.testwa.distest.client.control.client.boost.MessageCallback;
import com.testwa.distest.client.control.client.boost.MessageException;
import io.grpc.testwa.device.LogcatEndRequest;
import io.grpc.testwa.device.RemoteClientStartRequest;
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
        byte[] b = (byte[])o;
        try {
            RemoteClientStartRequest request = RemoteClientStartRequest.parseFrom(b);
            String serial = request.getSerial();
            String controller = request.getController();
            String url = env.getProperty("agent.socket.url");
            String username = env.getProperty("username");
            String password = env.getProperty("password");
            String wsUrl = String.format("%s?username=%s&password=%s", url, username, password);
            RemoteClient remoteClient = new RemoteClient(wsUrl, controller, serial);
            Clients.add(serial, remoteClient);
        } catch (InvalidProtocolBufferException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }


    }

}
