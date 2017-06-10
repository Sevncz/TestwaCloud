package com.testwa.distest.client.control.client.boost;

import com.testwa.distest.client.control.client.MainClient;
import io.socket.emitter.Emitter;

/**
 * Created by wen on 10/06/2017.
 */
public class Message {
    public static void on(String channelName, final MessageCallback callbackObject){
        MainClient.getWs().on(channelName, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                System.out.println(channelName + "callback called");
                try {
                    callbackObject.done(args[0], null);
                } catch (MessageException e) {
                    e.printStackTrace();
                }
            }
        });
        MainClient.getWs().emit("join-custom-channel", channelName );
    }


    public static void publish(String channelName, Object data ) throws MessageException {
        MainClient.getWs().emit(channelName, data );
    }

    public static void off(String channelName, final MessageCallback callbackObject) throws MessageException {
//        if(TestwaApp.getAppId() == null){
//            throw new TestwaException("TestwaApp id is null");
//        }
//        if(TestwaApp.getAppKey() == null){
//            throw new TestwaException("TestwaApp key is null");
//        }
        MainClient.getWs().disconnect();
        MainClient.getWs().emit("leave-custom-channel", channelName );
        MainClient.getWs().disconnect();
        MainClient.getWs().off(channelName, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                try {
                    callbackObject.done(null, null);
                } catch (MessageException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
