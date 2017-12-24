package com.testwa.distest.server.websocket.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.testwa.core.WebsocketEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 2016/9/24.
 */
@Log4j2
@Component
public class ScriptStopFeedbackHandler {

    @OnEvent(value = WebsocketEvent.FB_SCRIPT_STOP)
    public void onSdetail(SocketIOClient client, byte[] data, AckRequest ackRequest) {
        /**
         * 纪录一个脚本执行的起止时间
         */
//            Agent.ReportSdetailFeedback rs = Agent.ReportSdetailFeedback.parseFrom(data);
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


    }

}
