package com.testwa.distest.worker.core.util;

import java.io.IOException;
import java.net.ServerSocket;

public class PortUtil {

    public static int getAvailablePort() throws IOException {
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
