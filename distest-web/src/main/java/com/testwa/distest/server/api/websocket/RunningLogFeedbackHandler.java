package com.testwa.distest.server.api.websocket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.testwa.distest.server.config.EventConstant;
import com.testwa.distest.server.model.TestwaProcedureInfo;
import com.testwa.distest.server.rpc.proto.Agent;
import io.grpc.testwa.testcase.RunningLogRequest;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RunningLogFeedbackHandler {
    private static final Logger log = LoggerFactory.getLogger(RunningLogFeedbackHandler.class);

    private final SocketIOServer server;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private StringRedisTemplate template;

    @Autowired
    public RunningLogFeedbackHandler(SocketIOServer server) {
        this.server = server;
    }

    @OnEvent(value = EventConstant.feedback_runningLog)
    public void onEvent(SocketIOClient client, byte[] data, AckRequest ackRequest) {

        try {
            RunningLogRequest request = RunningLogRequest.parseFrom(data);
            log.info("action -----> {}", request.getActionBytes().toStringUtf8());
            TestwaProcedureInfo procedureInfo = new TestwaProcedureInfo();
            procedureInfo.toEntity(request);
            String json = mapper.writeValueAsString(procedureInfo);
            log.info("json -----> {}", json);
            template.opsForList().leftPush(EventConstant.feedback_runningLog, json);
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

    @OnEvent(value = EventConstant.feedback_scriptend)
    public void scriptEndEvent(SocketIOClient client, byte[] data, AckRequest ackRequest){
        try {
            Agent.RunningLogFeedbackEnd end = Agent.RunningLogFeedbackEnd.parseFrom(data);
            JsonFormat jf = new JsonFormat();
            String fbJson = jf.printToString(end);
            template.opsForList().leftPush(EventConstant.feedback_scriptend, fbJson);
        } catch (InvalidProtocolBufferException e) {
            log.error("InvalidProtocolBufferException", e);
        }


    }

}