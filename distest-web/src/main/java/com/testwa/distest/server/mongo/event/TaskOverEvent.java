package com.testwa.distest.server.mongo.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * Created by wen on 09/09/2017.
 */
@Data
public class TaskOverEvent extends ApplicationEvent {

    private Long taskCode;

    public TaskOverEvent(Object source, Long taskCode) {
        super(source);
        this.taskCode = taskCode;
    }

}
