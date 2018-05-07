package com.testwa.distest.client.control.boost;

import com.testwa.distest.client.control.client.MainSocket;
import io.socket.emitter.Emitter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wen on 10/06/2017.
 */
@Slf4j
public class Message {
    public static void on(String channelName, final MessageCallback callbackObject){
        MainSocket.getSocket().on(channelName, args -> {
            log.info("{} 消息接收", channelName);
            try {
                callbackObject.done(args[0], null);
            } catch (MessageException e) {
                e.printStackTrace();
            }
        });
        MainSocket.getSocket().emit("join-custom-channel", channelName );
    }


    public static void publish(String channelName, Object data ) throws MessageException {
        MainSocket.getSocket().emit(channelName, data );
    }

    public static void off(String channelName, final MessageCallback callbackObject) throws MessageException {
//        if(TestwaApp.getAppId() == null){
//            throw new TestwaException("TestwaApp id is null");
//        }
//        if(TestwaApp.getAppKey() == null){
//            throw new TestwaException("TestwaApp key is null");
//        }
        MainSocket.getSocket().disconnect();
        MainSocket.getSocket().emit("leave-custom-channel", channelName );
        MainSocket.getSocket().disconnect();
        MainSocket.getSocket().off(channelName, new Emitter.Listener() {
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
