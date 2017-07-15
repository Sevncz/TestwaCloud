package com.testwa.distest.server.websocket.service;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.client.rpc.proto.Agent;
import com.testwa.distest.server.mvc.model.Report;
import com.testwa.distest.server.mvc.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestcaseExcuteErrorFeedbackHandler {
    private static final Logger log = LoggerFactory.getLogger(TestcaseExcuteErrorFeedbackHandler.class);

    private final SocketIOServer server;

    @Autowired
    private ReportService reportService;

    @Autowired
    public TestcaseExcuteErrorFeedbackHandler(SocketIOServer server) {
        this.server = server;
    }

    @OnEvent(value = WebsocketEvent.FB_APPIUM_ERROR)
    public void onAppiumError(SocketIOClient client, byte[] data, AckRequest ackRequest) {

        log.info("receive message for appium schedule");
        try {
            Agent.AppiumRunErrorFeedback feedback = Agent.AppiumRunErrorFeedback.parseFrom(data);
            String reportDetailId = feedback.getReportDetailId();
            Report report = reportService.getReportById(reportDetailId);
            if(report == null){
                log.error("This reportId was not found", reportDetailId);
                return;
            }
            report.putErrorInfo("APPIUM", feedback.getErrormsg());
            reportService.save(report);

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

}