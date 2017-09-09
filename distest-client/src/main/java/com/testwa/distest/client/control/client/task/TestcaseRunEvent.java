package com.testwa.distest.client.control.client.task;

import com.testwa.core.model.RemoteRunCommand;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * Created by wen on 19/08/2017.
 */
@Data
public class TestcaseRunEvent extends ApplicationEvent {
    private RemoteRunCommand cmd;

    public TestcaseRunEvent(Object source, RemoteRunCommand cmd) {
        super(source);
        this.cmd = cmd;
    }

}
