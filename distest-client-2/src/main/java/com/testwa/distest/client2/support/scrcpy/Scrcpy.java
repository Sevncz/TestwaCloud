package com.testwa.distest.client2.support.scrcpy;

import com.testwa.distest.client2.support.android.ADBTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.*;

/**
 * @author wen
 * @create 2019-05-09 12:11
 */
@Slf4j
@Data
public class Scrcpy {
    private static final String SOCK_NAME = "scrcpy";

    private ServerSocket scrcpySocket;
    private Server scrcpyServer;
    /** android 临时文件存放目录 */
    private static final String ANDROID_TMP_DIR = "/data/local/tmp/";
    private static final String FILE_NAME = "scrcpy-server.jar";
    private String scrcpyServerPath;
    private String remoteScrcpyServerFile;
    private String deviceId;
    private int port;
    private String host = "127.0.0.1";


    private String maxSize;
    private String bitRate;
    private Boolean tunnelForward;
    private Boolean tunnelEnabled;
    private String crop;
    private Boolean sendFrameMeta;


    public Scrcpy(String filePath, String deviceId){
        this.deviceId = deviceId;
        this.scrcpyServerPath = filePath;
        this.remoteScrcpyServerFile = ANDROID_TMP_DIR + FILE_NAME;
        this.port = 28181;

        this.maxSize = "0";
        this.bitRate = "8000000";
        this.tunnelForward = false;
        this.crop = "-";
        this.sendFrameMeta = false;
    }

    private String getCommand() {
        return String.format("CLASSPATH=\"%s\" app_process / com.genymobile.scrcpy.Server %s %s %s %s %s", remoteScrcpyServerFile, maxSize, bitRate, tunnelForward, crop, sendFrameMeta);
    }


    private boolean pushServer(String deviceId) {
        log.info("push scrcpy servert {} to {}", scrcpyServerPath, remoteScrcpyServerFile);
        return ADBTools.pushFile(deviceId, scrcpyServerPath, remoteScrcpyServerFile);
    }


    private boolean enableTunnel(String deviceId, int port){
        if(enableTunnelReverse(deviceId, port)){
            return true;
        }
        log.warn("'adb reverse' failed, fallback to 'adb forward'");
        return enableTunnelForward(deviceId, port);
    }

    private boolean enableTunnelReverse(String deviceId, int port){
        return ADBTools.reverse(deviceId, SOCK_NAME, port);
    }

    private void disableTunnelReverse(String deviceId){
        ADBTools.reverseRemove(deviceId, SOCK_NAME);
    }

    private boolean enableTunnelForward(String deviceId, int port){
        return ADBTools.forward(deviceId, port, SOCK_NAME);
    }

    private boolean disableTunnelForward(String deviceId, int port){
        return ADBTools.forwardRemove(deviceId, port);
    }

    private void disableTunnel() {
        if (this.tunnelForward) {
            disableTunnelForward(deviceId, port);
        } else {
            disableTunnelReverse(deviceId);
        }
    }

    private void closeServerSocket() {
        if(this.scrcpySocket != null) {
            IOUtils.closeQuietly(this.scrcpySocket);
        }
    }

    private void closeServer() {
        if(this.scrcpyServer != null) {
            this.scrcpyServer.close();
        }
    }


    public boolean serverStart(){
        // push scrcpy server
        if(!pushServer(this.deviceId)){
            return false;
        }

        if (!enableTunnel(this.deviceId, port)) {
            return false;
        }

        if(!this.tunnelForward) {
            try {
                this.scrcpySocket = new ServerSocket(port);
                log.info("------------- Starting Scrcpy Server Up -------------");
            } catch (IOException e) {
                log.error("start scrcpy socket for {} error", port, e);
                disableTunnel();
                return false;
            }
        }

        this.scrcpyServer = new Server(this.deviceId, this.getCommand());
        this.scrcpyServer.start();
        if(!this.scrcpyServer.isRunning()) {
            if(!this.tunnelForward) {
                closeServerSocket();
            }
            disableTunnel();
            return false;
        }
        this.tunnelEnabled = true;
        return true;
    }

    public void serverStop(){
        closeServerSocket();
        closeServer();
        if(this.tunnelEnabled) {
            disableTunnel();
        }
    }

    public Socket serverConnectTo() {
        Socket deviceSocket = null;
        if(!this.tunnelForward) {
            try {
                deviceSocket = scrcpySocket.accept();
                log.info("------------- Starting Scrcpy Client Up -------------");
            } catch (IOException e) {
                log.error("start scrcpy socket client for {} error", port, e);
            }
        }else{
            try {
                deviceSocket = openSocket(this.host, this.port);
                log.info("------------- Starting Scrcpy Client Up -------------");
            } catch (Exception e) {
                log.error("start scrcpy socket client for {} error", port, e);
            }
        }
        if(deviceSocket == null) {
            return null;
        }

        if(!this.tunnelForward) {
            closeServerSocket();
        }
        disableTunnel();
        this.tunnelEnabled = false;
        return deviceSocket;
    }

    private Socket openSocket(String server, int port) throws Exception{
        Socket socket;
        // create a socket with a timeout
        try {
            InetAddress inteAddress = InetAddress.getByName(server);
            SocketAddress socketAddress = new InetSocketAddress(inteAddress, port);
            socket = new Socket();
            // 10 seconds
            int timeoutInMs = 10*1000;
            socket.connect(socketAddress, timeoutInMs);
            return socket;
        } catch (SocketTimeoutException ste) {
            log.error("Timed out waiting for the socket.");
            ste.printStackTrace();
            throw ste;
        }
    }

    public boolean isRunning() {
        return this.scrcpyServer.isRunning();
    }
}
