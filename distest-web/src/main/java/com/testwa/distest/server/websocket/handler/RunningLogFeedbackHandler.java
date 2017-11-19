package com.testwa.distest.server.websocket.handler;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.client.rpc.proto.Agent;
import com.testwa.core.redis.RedisCacheManager;
import com.testwa.distest.server.mvc.model.ProcedureInfo;
import io.rpc.testwa.task.ProcedureInfoRequest;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Log4j2
@Component
public class RunningLogFeedbackHandler {

    @Autowired
    private RedisCacheManager redisCacheMgr;


    @OnEvent(value = WebsocketEvent.FB_RUNNGING_LOG)
    public void onEvent(SocketIOClient client, byte[] data, AckRequest ackRequest) {

        try {
            ProcedureInfoRequest request = ProcedureInfoRequest.parseFrom(data);
            log.info("action -----> {}", request.getActionBytes().toStringUtf8());
            ProcedureInfo procedureInfo = new ProcedureInfo();
            procedureInfo.toEntity(request);
            String json = JSON.toJSONString(procedureInfo);
            log.info("json -----> {}", json);
            redisCacheMgr.lpush(WebsocketEvent.FB_RUNNGING_LOG, json);
        } catch (InvalidProtocolBufferException e) {
            log.error("InvalidProtocolBufferException", e);
        }
    }

    @OnEvent(value = WebsocketEvent.FB_SCRIPT_END)
    public void scriptEndEvent(SocketIOClient client, byte[] data, AckRequest ackRequest){
        try {
            Agent.RunningLogFeedbackEnd end = Agent.RunningLogFeedbackEnd.parseFrom(data);
            JsonFormat jf = new JsonFormat();
            String fbJson = jf.printToString(end);
            redisCacheMgr.lpush(WebsocketEvent.FB_SCRIPT_END, fbJson);
        } catch (InvalidProtocolBufferException e) {
            log.error("InvalidProtocolBufferException", e);
        }


    }

}