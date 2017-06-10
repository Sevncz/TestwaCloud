package com.testwa.distest.client.boost.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.client.appium.manager.AppiumCache;
import com.testwa.distest.client.boost.TestwaException;
import com.testwa.distest.client.boost.TestwaNotificationCallback;
import com.testwa.distest.client.control.client.boost.MessageException;
import com.testwa.distest.client.service.HttpService;
import com.testwa.distest.client.task.Testcase;
import com.testwa.distest.client.task.TestcaseTaskCaches;
import com.testwa.distest.client.rpc.proto.Agent;
import io.socket.client.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;

/**
 * Created by wen on 16/8/14.
 */
@Component
public class RunTestcaseCallbackImpl implements TestwaNotificationCallback{
    private static Logger LOG = LoggerFactory.getLogger(RunTestcaseCallbackImpl.class);

    private static final String channel = WebsocketEvent.FB_APPIUM_ERROR;

    @Value("${agent.web.url}")
    private String agentWebUrl;

    @Autowired
    private Socket socket;

    @Autowired
    private HttpService httpService;

    @Override
    public void done(Object o, TestwaException e) throws MessageException {
        /**
         * 运行一个测试案例
         * 需要参数?
         *  (异步)
         * 1. app文件
         * 2. appId,检查是否有这个app,如果没有则下载
         * 3. 脚本文件(多个)
         * 4. 下载脚本,不管有没有,因为脚本会被修改
         * 5. testcaseId,需要传给appium
         * 6. testcaseLogId,需要传给appium
         * 7. serial,设备id
         * 8. installapp
         */
        byte[] b = (byte[])o;
        Long start = System.currentTimeMillis();
        try {
            Agent.TestcaseMessage testcaseMessage = Agent.TestcaseMessage.parseFrom(b);
            String reportDetailId = testcaseMessage.getReportDetailId();
            String serial = testcaseMessage.getSerial();
            String install = testcaseMessage.getInstall();
            String appId = testcaseMessage.getAppId();
            List<String> scriptIds = testcaseMessage.getScriptIdsList();
            int frequency = testcaseMessage.getFrequency();

            if(AppiumCache.device_running.contains(serial)){
                Agent.AppiumRunErrorFeedback fb = Agent.AppiumRunErrorFeedback.newBuilder()
                        .setErrormsg("Device is running case.")
                        .setReportDetailId(reportDetailId)
                        .build();
                socket.emit(channel, fb);
                LOG.error("Device {} busy.", serial);
                return;
            }

            Testcase tc = new Testcase(appId, serial, scriptIds, reportDetailId, install, agentWebUrl, httpService);
            TestcaseTaskCaches.add(serial, tc);
            try {
                tc.runAppium();
                LOG.info("Appium was running");
                URL appiumUrl = tc.getAp().appiumMan.getAppiumUrl();
                String url = appiumUrl.toString().replace("0.0.0.0", "127.0.0.1");

                AppiumCache.apt.put(serial, tc.getAp());
                AppiumCache.url.put(serial, url);
                AppiumCache.device_running.add(serial);

            } catch (Exception exc) {
                LOG.error("Error in start appium  ", exc);
                Agent.AppiumRunErrorFeedback fb = Agent.AppiumRunErrorFeedback.newBuilder()
                        .setErrormsg(exc.toString())
                        .setReportDetailId(reportDetailId)
                        .build();
                socket.emit(channel, fb);
                return;
            }

            tc.runScripts();
        } catch (InvalidProtocolBufferException e1) {
            LOG.error("Error before start appium  ", e);
            Agent.AppiumRunErrorFeedback fb = Agent.AppiumRunErrorFeedback.newBuilder()
                    .setErrormsg(e.toString())
                    .setReportDetailId("xxxxxxxx")
                    .build();
            socket.emit(channel, fb);
        }finally {
            Long end = System.currentTimeMillis();
            LOG.info("Time: {}", end-start);
        }
    }
}
