package com.testwa.distest.client.boost.impl;

import com.testwa.distest.client.boost.TestwaException;
import com.testwa.distest.client.boost.TestwaNotificationCallback;
import com.testwa.distest.client.control.client.boost.MessageException;

/**
 * Created by wen on 16/8/14.
 */
//@Component
public class TestwaNotificationCallbackImpl implements TestwaNotificationCallback{
    @Override
    public void done(Object o, TestwaException e) throws MessageException {
        System.out.println("TestwaNotificationCallbackImpl");

    }
}
