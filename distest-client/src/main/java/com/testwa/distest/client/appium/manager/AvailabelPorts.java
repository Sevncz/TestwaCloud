package com.testwa.distest.client.appium.manager;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wen on 16/5/28.
 */
public class AvailabelPorts {

    public static Map<String, Integer> used = new HashMap<String, Integer>();//设备和使用过的端口对应关系

    /*
     * Generates Random ports
     * Used during starting appium server
     */
    public int getPort() throws Exception {
        ServerSocket socket = new ServerSocket(0);
        socket.setReuseAddress(true);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }

}
