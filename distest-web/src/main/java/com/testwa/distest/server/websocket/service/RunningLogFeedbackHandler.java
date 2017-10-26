package com.testwa.distest.server.websocket.service;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.testwa.core.WebsocketEvent;
import com.testwa.distest.client.rpc.proto.Agent;
import com.testwa.distest.redis.RedisCacheManager;
import com.testwa.distest.server.mvc.model.ProcedureInfo;
import io.rpc.testwa.task.ProcedureInfoRequest;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RunningLogFeedbackHandler {
    private static final Logger log = LoggerFactory.getLogger(RunningLogFeedbackHandler.class);

    private final SocketIOServer server;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RedisCacheManager redisCacheMgr;

    @Autowired
    public RunningLogFeedbackHandler(SocketIOServer server) {
        this.server = server;
    }

    @OnEvent(value = WebsocketEvent.FB_RUNNGING_LOG)
    public void onEvent(SocketIOClient client, byte[] data, AckRequest ackRequest) {

        try {
            ProcedureInfoRequest request = ProcedureInfoRequest.parseFrom(data);
            log.info("action -----> {}", request.getActionBytes().toStringUtf8());
            ProcedureInfo procedureInfo = new ProcedureInfo();
            procedureInfo.toEntity(request);
            String json = mapper.writeValueAsString(procedureInfo);
            log.info("json -----> {}", json);
            redisCacheMgr.lpush(WebsocketEvent.FB_RUNNGING_LOG, json);
        } catch (InvalidProtocolBufferException e) {
            log.error("InvalidProtocolBufferException", e);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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