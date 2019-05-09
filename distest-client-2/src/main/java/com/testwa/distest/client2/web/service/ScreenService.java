package com.testwa.distest.client2.web.service;

import com.testwa.distest.client2.support.scrcpy.Scrcpy;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;


/**
 * @author wen
 * @create 2019-05-08 15:14
 */
@Slf4j
@Service
public class ScreenService {

    @Value("${scrcpy-server.path}")
    private String scrcpyServerPath;

    public void scrcpyStart(String deviceId) {
        Scrcpy scrcpy = new Scrcpy(scrcpyServerPath, deviceId);
        scrcpy.serverStart();
        Socket socket = scrcpy.serverConnectTo();
        if(scrcpy.isRunning() && socket != null){
            try (DataInputStream dIn = new DataInputStream(socket.getInputStream())) {
                while (scrcpy.isRunning()) {
                    int length = 1024;
                    byte[] message = new byte[length];
                    int i;
                    while((i = dIn.read(message)) > -1) {
                        log.info("length: {}", i);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
