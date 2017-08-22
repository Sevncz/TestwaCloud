package com.testwa.distest.client.control.client;

import com.testwa.distest.client.control.client.boost.Message;
import com.testwa.distest.client.control.client.boost.MessageCallback;
import com.testwa.distest.client.model.UserInfo;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

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
//                                AgentSystem agentSystem = new AgentSystem();
//                                Agent.SystemInfo systemInfo = agentSystem.toAgentSystemInfo(env.getProperty("agent.key"));
//                                MainSocket.getSocket().emit("agentRegister", systemInfo.toByteArray());
            }).on(Socket.EVENT_DISCONNECT, objects -> log.info("websocket was disconnected"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void receive(String channelName, MessageCallback callbackObject){
        Message.on(channelName, callbackObject);
    }
}
