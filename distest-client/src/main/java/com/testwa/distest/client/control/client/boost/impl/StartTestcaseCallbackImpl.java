package com.testwa.distest.client.control.client.boost.impl;

import com.alibaba.fastjson.JSON;
import com.testwa.core.cmd.RemoteRunCommand;
import com.testwa.distest.client.control.client.boost.MessageCallback;
import com.testwa.distest.client.control.client.boost.MessageException;
import com.testwa.distest.client.control.client.task.TestcaseRunEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


@Component
public class StartTestcaseCallbackImpl implements MessageCallback {
    private String token;

    @Autowired
    ApplicationContext context;

    @Override
    public void done(Object o, MessageException e) throws MessageException {
        String msg = (String) o;
        RemoteRunCommand cmd = JSON.parseObject(msg, RemoteRunCommand.class);
        context.publishEvent(new TestcaseRunEvent(this, cmd));
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }
}
