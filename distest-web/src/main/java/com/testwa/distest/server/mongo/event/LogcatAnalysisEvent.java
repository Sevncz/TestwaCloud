package com.testwa.distest.server.mongo.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * Created by wen on 09/09/2017.
 */
@Data
public class LogcatAnalysisEvent extends ApplicationEvent {

    private String deviceId;
    private Long taskCode;

    public LogcatAnalysisEvent(Object source, String deviceId, Long taskCode) {
        super(source);
        this.taskCode = taskCode;
        this.deviceId = deviceId;
    }

}
