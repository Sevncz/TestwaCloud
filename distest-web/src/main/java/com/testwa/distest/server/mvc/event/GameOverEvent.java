package com.testwa.distest.server.mvc.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * Created by wen on 09/09/2017.
 */
@Data
public class GameOverEvent extends ApplicationEvent {

    private String exeId;

    public GameOverEvent(Object source, String exeId) {
        super(source);
        this.exeId = exeId;
    }

}
