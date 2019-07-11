package com.testwa.distest.client.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

public class PortUtil {

    public static int getAvailablePort() throws IOException {
//        int port = 0;
//        do {
//            Random rnd = new Random();
//            port = rnd.nextInt(1024, 65535);
//        } while (!isPortAvailable(port));

        ServerSocket s = new ServerSocket(0);

        int port = s.getLocalPort();
        s.close();
        return port;
    }

    private static boolean isPortAvailable(final int port) throws IOException {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            return true;
        } catch (final IOException e) {
        } finally {
            if (ss != null) {
                ss.close();
            }
        }

        return false;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(getAvailablePort());
    }

}
