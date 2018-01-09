package com.testwa.distest.client.control.client;

import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.android.impl.AndroidDeviceStore;
import com.testwa.distest.client.ApplicationContextUtil;
import com.testwa.distest.client.control.client.boost.Message;
import com.testwa.distest.client.control.client.boost.MessageCallback;
import com.testwa.distest.client.control.event.DeviceConnectedEvent;
import com.testwa.distest.client.control.event.WebSocketConnectedEvent;
import com.testwa.distest.client.control.event.WebSocketDisconnectedEvent;
import com.testwa.distest.client.model.UserInfo;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.net.URISyntaxException;
import java.util.TreeSet;

/**
 * Created by wen on 10/06/2017.
 */
public class MainSocket extends BaseClient{
    private static Logger log = LoggerFactory.getLogger(MainSocket.class);

    private static Socket socket;

    public static Socket getSocket(){
        return socket;
    }

    public static void connect(String url, String token) {
        Socket socket = null;
        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnectionDelay = 1000; //失败重连的时间间隔
        opts.timeout = 500; //连接超时时间(ms)
        try {
            socket = IO.socket(String.format("%s?token=%s&type=client", url, token), opts);
            socket.connect();
            MainSocket.socket = socket;
            MainSocket.socket.on(Socket.EVENT_CONNECT, objects -> {
                log.info("websocket was connected");
                ApplicationContext context = ApplicationContextUtil.getApplicationContext();
                context.publishEvent(new WebSocketConnectedEvent(MainSocket.class));
            }).on(Socket.EVENT_DISCONNECT, objects -> {
                log.info("websocket was disconnected");
                ApplicationContext context = ApplicationContextUtil.getApplicationContext();
                context.publishEvent(new WebSocketDisconnectedEvent(MainSocket.class));
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void receive(String channelName, MessageCallback callbackObject){
        Message.on(channelName, callbackObject);
    }
}
