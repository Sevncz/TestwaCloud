package com.testwa.distest.client.control.client;

import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;

/**
 * Created by wen on 10/06/2017.
 */
@Slf4j
public class MainSocket extends BaseClient{
    private static Socket socket;

    public static Socket getSocket(){
        return socket;
    }

    public static void connect(String url, String token) {
        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnectionDelay = 3000; //失败重连的时间间隔
        opts.timeout = 1000; //连接超时时间(ms)
        try {
            Socket socket = IO.socket(String.format("%s?token=%s&type=client", url, token), opts);
            socket.connect();
            MainSocket.socket = socket;
            MainSocket.socket.on(Socket.EVENT_CONNECT, objects -> {
                log.debug("客户端已连接");
            }).on(Socket.EVENT_DISCONNECT, objects -> {
                log.debug("客户端连接断开");
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
