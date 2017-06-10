package com.testwa.distest.client.control.client;

import com.testwa.distest.client.control.client.boost.Message;
import com.testwa.distest.client.control.client.boost.MessageCallback;
import io.socket.client.Socket;

/**
 * Created by wen on 10/06/2017.
 */
public class MainClient extends BaseClient{
    public String username;
    public String password;
    private static Socket ws;

    public static Socket getWs() {
        return ws;
    }

    public static void setWs(Socket socket) {
        MainClient.ws = socket;
    }

    public MainClient(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void connect() {
        if(MainClient.getWs() != null){
            MainClient.getWs().connect();
        }
    }

    public void disconnect(){
        if(MainClient.getWs() != null) {
            MainClient.getWs().disconnect();
        }
    }


    public void receive(String channelName, MessageCallback callbackObject){
        Message.on(channelName, callbackObject);
    }
}
