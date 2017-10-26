package com.testwa.distest.server.websocket.service;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.client.rpc.proto.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 2016/9/24.
 */


@Component
public class ReportSdetailFeedbackHandler {
    private static final Logger log = LoggerFactory.getLogger(ReportSdetailFeedbackHandler.class);

    private final SocketIOServer server;

    @Autowired
    public ReportSdetailFeedbackHandler(SocketIOServer server) {
        this.server = server;
    }

    @OnEvent(value = WebsocketEvent.FB_REPORT_SDETAIL)
    public void onSdetail(SocketIOClient client, byte[] data, AckRequest ackRequest) {
        /**
         * 纪录一个脚本执行的起止时间
         */
        try {
            Agent.ReportSdetailFeedback rs = Agent.ReportSdetailFeedback.parseFrom(data);
//            ReportSdetail sdetail = reportSdetailService.findTestcaseSdetailByDetailIdScriptId(rs.getReportDetailId(), rs.getScriptId());
//            log.info("receive message for sdetail message, sdetailId: {}.", sdetail.getId());
//            if(Agent.ReportSdetailType.start.name().equals(rs.getType().name())){
//                sdetail.setStartTime(new Date());
//                sdetail.setMachineName(rs.getMatchineName());
//                reportSdetailService.save(sdetail);
//            }
//            if(Agent.ReportSdetailType.end.name().equals(rs.getType().name())){
//                sdetail.setEndTime(new Date());
//                reportSdetailService.save(sdetail);
//            }

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

}
