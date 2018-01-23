package com.testwa.distest.server.mongo.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * Created by wen on 09/09/2017.
 */
@Data
public class GameOverEvent extends ApplicationEvent {

    private Long taskId;

    public GameOverEvent(Object source, Long taskId) {
        super(source);
        this.taskId = taskId;
    }

}
