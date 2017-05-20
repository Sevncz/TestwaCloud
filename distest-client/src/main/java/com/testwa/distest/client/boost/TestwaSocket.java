package com.testwa.distest.client.boost;

import io.socket.client.Socket;

/**
 * Created by wen on 16/8/14.
 */
public class TestwaSocket {
    private static Socket socket;

    public static Socket getSocket() {
        return socket;
    }

    public static void setSocket(Socket socket) {
        TestwaSocket.socket = socket;

    }
}
