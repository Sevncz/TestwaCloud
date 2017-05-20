package com.testwa.distest.client.boost;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by wen on 16/8/14.
 */
public class TestwaApp {
    public String agentId;
    public String agentKey;

    public TestwaApp(String agentId, String agentKey) {
        this.agentId = agentId;
        this.agentKey = agentKey;
    }

    /**
     * gives the App ID to connect to
     * @return agentId
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * returns the authentication key to connect to the App, every App created in cloudboost has an ID
     * and client key
     * @return
     */
    public String getAgentKey() {
        return agentKey;
    }

    public void connect(){
        TestwaSocket.getSocket().connect();
    }

    public void disconnect(){
        TestwaSocket.getSocket().disconnect();
    }

    public void onDisconnect(){
        TestwaSocket.getSocket().on(Socket.EVENT_DISCONNECT,new Emitter.Listener() {

            @Override
            public void call(Object... args) {

            }
        });
    }

    public void receive(String channelName, TestwaNotificationCallback callbackObject){
        TestwaNotification.on(channelName, callbackObject);
    }

}
