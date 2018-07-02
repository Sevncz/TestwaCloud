package com.testwa.distest.server.mongo.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * Created by wen on 09/09/2017.
 */
@Data
public class TaskOverEvent extends ApplicationEvent {

    private Long taskCode;
    private boolean timeout;

    public TaskOverEvent(Object source, Long taskCode, boolean timeout) {
        super(source);
        this.taskCode = taskCode;
        this.timeout = timeout;
    }

}
