package com.testwa.distest.client.boost.impl;

import com.testwa.distest.client.boost.TestwaException;
import com.testwa.distest.client.boost.TestwaNotificationCallback;
import com.testwa.distest.client.control.client.boost.MessageException;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 16/8/20.
 */
@Component
public class UninstallAppCallbackImpl implements TestwaNotificationCallback {


    @Override
    public void done(Object o, TestwaException e) throws MessageException {
        /**
         * 卸载app
         */
    }
}
