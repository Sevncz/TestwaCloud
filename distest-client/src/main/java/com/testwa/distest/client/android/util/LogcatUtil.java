package com.testwa.distest.client.android.util;

import com.google.protobuf.ByteString;
import com.testwa.distest.client.rpc.client.LogcatClient;
import com.testwa.core.service.AdbDriverService;
import com.testwa.core.service.LogcatServiceBuilder;
import io.grpc.testwa.device.LogcatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Created by wen on 17/12/2016.
 */
public class LogcatUtil {

    private static Logger LOG = LoggerFactory.getLogger(LogcatUtil.class);

    private AdbDriverService service;
    private LogcatClient logcatClient;
    private PipedOutputStream outputStream;
    private PipedInputStream inputStream;
    private String deviceId;
    private boolean isRunning = false;



    public LogcatUtil(String host, Integer port, String deviceId) {
        this.logcatClient = new LogcatClient(host, port);
        this.outputStream = new PipedOutputStream();
        this.inputStream = new PipedInputStream();
        this.deviceId = deviceId;

        try {
            inputStream.connect(outputStream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void startLogcatListener(String level, String tag, String filter){
        this.isRunning = true;

        this.service = new LogcatServiceBuilder()
                .whithDeviceId(this.deviceId)
                .whithLevel(level)
                .whithTag(tag)
                .whithFilter(filter)
                .build();
        this.service.addOutPutStream(this.outputStream);
        this.service.start();

        new Thread(() -> {
            while(this.isRunning){
                byte[] buf=new byte[2048];
                int count = 0;
                try {
                    while (count == 0) {
                        count = inputStream.available();
                    }
                    int readCount = 0; // 已经成功读取的字节的个数
                    while (readCount < count) {
                        readCount += inputStream.read(buf, readCount, count - readCount);

                        new Thread(() -> {
                            LogcatRequest request = LogcatRequest.newBuilder()
                                    .setContent(ByteString.copyFrom(buf))
                                    .setSerial(deviceId)
                                    .build();
                            logcatClient.sender(request);
                            LOG.info("sender ---====--=-=-=-=-=-=-=-=-=-=");
                        }).start();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stopLogcatListener() {
        this.isRunning = false;

        if(service != null && service.isRunning()){
            service.stop();
        }

        try {
            if(this.inputStream != null){
                this.inputStream.close();
            }
            if(this.outputStream != null){
                this.outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
