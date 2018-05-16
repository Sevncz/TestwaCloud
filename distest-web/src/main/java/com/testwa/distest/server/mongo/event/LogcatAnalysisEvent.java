package com.testwa.distest.server.mongo.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * Created by wen on 09/09/2017.
 */
@Data
public class LogcatAnalysisEvent extends ApplicationEvent {

    private String deviceId;
    private Long taskId;

    public LogcatAnalysisEvent(Object source, String deviceId, Long taskId) {
        super(source);
        this.taskId = taskId;
        this.deviceId = deviceId;
    }

}
