package com.testwa.distest.client.component.port;

import com.testwa.core.tools.PortProvider;

/**
 * Created by wen on 13/08/2017.
 */
public class AppiumPortProvider {
    private static PortProvider portProvider;

    public static void init(int portStart,int portEnd){
        portProvider = new PortProvider(portStart,portEnd);
    }

    public synchronized static int pullPort(){
        return portProvider.pullPort();
    }

    public synchronized static boolean pushPort(int port){
        return portProvider.pushPort(port);
    }
}
