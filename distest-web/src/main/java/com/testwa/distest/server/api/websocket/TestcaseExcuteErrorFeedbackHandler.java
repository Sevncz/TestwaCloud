package com.testwa.distest.server.api.websocket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.testwa.distest.server.config.EventConstant;
import com.testwa.distest.server.model.TestwaReport;
import com.testwa.distest.server.rpc.proto.Agent;
import com.testwa.distest.server.service.TestwaReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestcaseExcuteErrorFeedbackHandler {
    private static final Logger log = LoggerFactory.getLogger(TestcaseExcuteErrorFeedbackHandler.class);

    private final SocketIOServer server;

    @Autowired
    private TestwaReportService testwaReportService;

    @Autowired
    public TestcaseExcuteErrorFeedbackHandler(SocketIOServer server) {
        this.server = server;
    }

    @OnEvent(value = EventConstant.feedback_appium_error)
    public void onAppiumError(SocketIOClient client, byte[] data, AckRequest ackRequest) {

        log.info("receive message for appium run");
        try {
            Agent.AppiumRunErrorFeedback feedback = Agent.AppiumRunErrorFeedback.parseFrom(data);
            String reportDetailId = feedback.getReportDetailId();
            TestwaReport report = testwaReportService.getReportById(reportDetailId);
            if(report == null){
                log.error("This reportId was not found", reportDetailId);
                return;
            }
            report.putErrorInfo("APPIUM", feedback.getErrormsg());
            testwaReportService.save(report);

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

}