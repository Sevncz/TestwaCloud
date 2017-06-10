package com.testwa.distest.client.web.startup;

import com.testwa.core.WebsocketEvent;
import com.testwa.distest.client.boost.TestwaNotificationCallback;
import com.testwa.distest.client.control.client.MainClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 监听事件初始化
 * Created by wen on 16/8/14.
 */
@Component
public class MainMessageRegister implements CommandLineRunner {
    private final MainClient client;

    @Autowired
    @Qualifier("runTestcaseCallbackImpl")
    private TestwaNotificationCallback tnotificationRunCB;

    @Autowired
    @Qualifier("screenCaptureStartCallbackImpl")
    private TestwaNotificationCallback screenCaptureStartCB;

    @Autowired
    @Qualifier("screenCaptureStopCallbackImpl")
    private TestwaNotificationCallback screenCaptureStopCB;

    @Autowired
    @Qualifier("logcatStartCallbackImpl")
    private TestwaNotificationCallback logcatStartCB;

    @Autowired
    @Qualifier("logcatStopCallbackImpl")
    private TestwaNotificationCallback logcatStopCB;

    @Autowired
    @Qualifier("installAppCallbackImpl")
    private TestwaNotificationCallback installAppCB;

    @Autowired
    @Qualifier("uninstallAppCallbackImpl")
    private TestwaNotificationCallback uninstallAppCB;

    @Autowired
    public MainMessageRegister(MainClient client) {
        this.client = client;
    }

    @Override
    public void run(String... strings) throws Exception {
//        app.receive("test_one_app", tnotificationCB);
        // 运行
//        client.receive(WebsocketEvent.ON_TESTCASE_RUN, tnotificationRunCB);
//        // 获得屏幕开始
//        client.receive(WebsocketEvent.ON_SCREEN_SHOW_START, screenCaptureStartCB);
//        // 获得屏幕结束
//        client.receive(WebsocketEvent.ON_SCREEN_SHOW_STOP, screenCaptureStopCB);
//        // 安装
//        client.receive(WebsocketEvent.ON_APP_INSTALL, installAppCB);
//        // 卸载
//        client.receive(WebsocketEvent.ON_APP_UNINSTALL, uninstallAppCB);
//        // 获得logcat开始
//        client.receive(WebsocketEvent.ON_LOGCAT_SHOW_START, logcatStartCB);
//        // 获得logcat结束
//        client.receive(WebsocketEvent.ON_LOGCAT_SHOW_STOP, logcatStopCB);


    }
}
