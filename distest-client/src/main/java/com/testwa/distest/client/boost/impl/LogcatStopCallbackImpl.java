package com.testwa.distest.client.boost.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import com.testwa.distest.client.boost.TestwaException;
import com.testwa.distest.client.boost.TestwaNotificationCallback;
import com.testwa.distest.client.task.AsyncTask;
import io.grpc.testwa.device.LogcatEndRequest;
import io.grpc.testwa.device.LogcatStartRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by wen on 16/8/14.
 */
@Component
public class LogcatStopCallbackImpl implements TestwaNotificationCallback{
    private static Logger LOG = LoggerFactory.getLogger(LogcatStopCallbackImpl.class);

    @Autowired
    private AsyncTask asyncTask;

    @Override
    public void done(Object o, TestwaException e) throws TestwaException {
        byte[] b = (byte[])o;
        try {
            LogcatEndRequest request = LogcatEndRequest.parseFrom(b);
            String serial = request.getSerial();
            asyncTask.logcatStop(serial);
        } catch (InvalidProtocolBufferException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }


    }
}
