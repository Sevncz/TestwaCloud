package com.testwa.distest.client.task;

import com.testwa.distest.client.android.AndroidHelper;
import com.testwa.distest.client.android.util.LogcatUtil;
import com.testwa.distest.client.android.util.MiniCapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wen on 16/9/4.
 */
@Component
public class AsyncTask {
    private static Logger LOG = LoggerFactory.getLogger(AsyncTask.class);
    private static Map<String, MiniCapUtil> miniMap = new HashMap<>();
    private static Map<String, LogcatUtil> logcatMap = new HashMap<>();

    @Autowired
    private Environment env;


    @Async
    public void screenCaptureStart(String serial){
        MiniCapUtil minicap = new MiniCapUtil(
                AndroidHelper.getInstance().getAndroidDevice(serial).getDevice(),
                env.getProperty("grpc.host"),
                Integer.parseInt(env.getProperty("grpc.port"))
        );
        minicap.startScreenListener();
        miniMap.put(serial, minicap);
    }


    @Async
    public void screenCaptureEnd(String serial){
        MiniCapUtil minicap = miniMap.remove(serial);
        minicap.stopScreenListener();
    }

    @Async
    public void logcatStart(String serial, String level, String tag, String filter) {
        LogcatUtil logcat = new LogcatUtil(env.getProperty("grpc.host"), Integer.parseInt(env.getProperty("grpc.port")), serial);
        logcat.startLogcatListener(level, tag, filter);
        logcatMap.put(serial, logcat);
    }

    @Async
    public void logcatStop(String serial) {
        LogcatUtil logcat = logcatMap.remove(serial);
        logcat.stopLogcatListener();
    }
}
