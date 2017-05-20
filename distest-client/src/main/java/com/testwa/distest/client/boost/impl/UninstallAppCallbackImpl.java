package com.testwa.distest.client.boost.impl;

import com.testwa.distest.client.boost.TestwaException;
import com.testwa.distest.client.boost.TestwaNotificationCallback;
import org.springframework.stereotype.Component;

/**
 * Created by wen on 16/8/20.
 */
@Component
public class UninstallAppCallbackImpl implements TestwaNotificationCallback {


    @Override
    public void done(Object o, TestwaException e) throws TestwaException {
        /**
         * 卸载app
         */
    }
}
