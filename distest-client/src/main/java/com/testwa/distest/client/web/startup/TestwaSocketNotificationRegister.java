package com.testwa.distest.client.web.startup;

import com.testwa.distest.client.boost.TestwaApp;
import com.testwa.distest.client.boost.TestwaNotificationCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 监听事件初始化
 * Created by wen on 16/8/14.
 */
@Component
public class TestwaSocketNotificationRegister implements CommandLineRunner {
    private final TestwaApp app;

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
    public TestwaSocketNotificationRegister(TestwaApp app) {
        this.app = app;
    }

    @Override
    public void run(String... strings) throws Exception {
//        app.receive("test_one_app", tnotificationCB);
        // 运行
        app.receive("testcaseRun", tnotificationRunCB);
        // 获得屏幕开始
        app.receive("screenStart", screenCaptureStartCB);
        // 获得屏幕结束
        app.receive("screenStop", screenCaptureStopCB);
        // 安装
        app.receive("installApp", installAppCB);
        // 卸载
        app.receive("uninstallApp", uninstallAppCB);
        // 获得logcat开始
        app.receive("logcatStart", logcatStartCB);
        // 获得logcat结束
        app.receive("logcatStop", logcatStopCB);
    }
}
