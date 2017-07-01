package com.testwa.distest.client.control.client;

import com.android.ddmlib.IDevice;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.model.UserInfo;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by wen on 03/06/2017.
 */
public class BaseClient {

    public final static void startRemoteClient(IDevice device){
        Environment env = ApplicationContextUtil.getApplicationContext().getEnvironment();
        String url = env.getProperty("agent.socket.url");
        String webHost = env.getProperty("grpc.host");
        Integer webPort = Integer.parseInt(env.getProperty("grpc.port"));
        String wsUrl = String.format("%s?token=%s&type=device&serial=%s", url, UserInfo.token, device.getSerialNumber());

        try {
            RemoteClient remoteClient = new RemoteClient(wsUrl, "", device.getSerialNumber(), webHost, webPort);
            Clients.add(device.getSerialNumber(), remoteClient);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
