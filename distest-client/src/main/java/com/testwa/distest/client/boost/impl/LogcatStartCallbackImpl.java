package com.testwa.distest.client.boost.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import com.testwa.distest.client.boost.TestwaException;
import com.testwa.distest.client.boost.TestwaNotificationCallback;
import com.testwa.distest.client.control.client.boost.MessageException;
import com.testwa.distest.client.task.AsyncTask;
import io.grpc.testwa.device.LogcatStartRequest;
import io.grpc.testwa.device.ScreenCaptureStartRequest;
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
public class LogcatStartCallbackImpl implements TestwaNotificationCallback{
    private static Logger LOG = LoggerFactory.getLogger(LogcatStartCallbackImpl.class);

    @Autowired
    private AsyncTask asyncTask;

    @Override
    public void done(Object o, TestwaException e) throws MessageException {
        byte[] b = (byte[])o;
        try {
            LogcatStartRequest request = LogcatStartRequest.parseFrom(b);
            String serial = request.getSerial();
            String level = request.getLevel();
            String tag = request.getTag();
            String filter = request.getFilter();
            asyncTask.logcatStart(serial, level, tag, filter);

        } catch (InvalidProtocolBufferException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }


    }
}
