package com.testwa.distest.client.boost;

import io.socket.emitter.Emitter;

/**
 * Created by wen on 16/8/14.
 */
public class TestwaNotification {

    public static void on(String channelName, final TestwaNotificationCallback callbackObject){
//        if(!TestwaSocket.getSocket().connected()){
//            TestwaSocket.getSocket().connect();
//        }
        TestwaSocket.getSocket().on(channelName, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                System.out.println(channelName + "callback called");
                try {
                    callbackObject.done(args[0], null);
                } catch (TestwaException e) {
                    e.printStackTrace();
                }
            }
        });
        TestwaSocket.getSocket().emit("join-custom-channel", channelName );
    }


    public static void publish(String channelName, Object data ) throws TestwaException {
//        if(TestwaApp.getAppId() == null){
//            throw new TestwaException("TestwaApp id is null");
//        }
//        if(TestwaApp.getAppKey() == null){
//            throw new TestwaException("TestwaApp key is null");
//        }
        TestwaSocket.getSocket().emit(channelName, data );
    }

    public static void off(String channelName, final TestwaMessageCallback callbackObject) throws TestwaException {
//        if(TestwaApp.getAppId() == null){
//            throw new TestwaException("TestwaApp id is null");
//        }
//        if(TestwaApp.getAppKey() == null){
//            throw new TestwaException("TestwaApp key is null");
//        }
        TestwaSocket.getSocket().disconnect();
        TestwaSocket.getSocket().emit("leave-custom-channel", channelName );
        TestwaSocket.getSocket().disconnect();
        TestwaSocket.getSocket().off(channelName, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                try {
                    callbackObject.done(null, null);
                } catch (TestwaException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
