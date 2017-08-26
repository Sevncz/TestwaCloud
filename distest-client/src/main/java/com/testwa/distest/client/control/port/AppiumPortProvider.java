package com.testwa.distest.client.control.port;

import com.testwa.core.PortProvider;

/**
 * Created by wen on 13/08/2017.
 */
public class AppiumPortProvider {
    private static PortProvider portProvider;

    public static void init(int portStart,int portEnd){
        portProvider = new PortProvider(portStart,portEnd);
    }

    public static int pullPort(){
        return portProvider.pullPort();
    }

    public static boolean pushPort(int port){
        return portProvider.pushPort(port);
    }
}
